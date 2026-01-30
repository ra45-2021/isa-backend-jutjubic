# Kako Sistem Radi - Detaljan Vodič

## Pregled Sistema

Sistem koristi **CRDT (Conflict-free Replicated Data Type)** G-counter pattern za brojanje pregleda videa kroz više replika. Svaka replika ima svoju odvojenu tabelu za brojanje, što omogućava nezavisno operisanje i kasnije spajanje rezultata.

## Ključne Komponente

### 1. ReplicaPhysicalNamingStrategy

**Lokacija:** `src/main/java/com/jutjubic/config/ReplicaPhysicalNamingStrategy.java`

Ova klasa presreće Hibernate proces kreiranja tabela i **dinamički menja ime SAMO za `video_view_crdt` tabelu**.

```java
public Identifier toPhysicalTableName(Identifier logicalName, JdbcEnvironment context) {
    // Prvo konvertuje camelCase -> snake_case (npr. emailAdress -> email_adress)
    Identifier result = delegate.toPhysicalTableName(logicalName, context);
    String tableName = result.getText();

    // Dodaj sufiks SAMO za video_view_crdt tabelu
    if ("video_view_crdt".equals(tableName)) {
        String newName = tableName + "_" + tableSuffix; // npr. video_view_crdt_replica1
        return Identifier.toIdentifier(newName);
    }

    return result; // Sve ostale tabele ostaju iste
}
```

**Rezultat:**
- `video_view_crdt` → `video_view_crdt_replica1` (za Repliku 1)
- `video_view_crdt` → `video_view_crdt_replica2` (za Repliku 2)
- `users` → `users` (ostaje isto)
- `posts` → `posts` (ostaje isto)

### 2. VideoViewCrdt Entity

**Lokacija:** `src/main/java/com/jutjubic/domain/VideoViewCrdt.java`

```java
@Entity
@Table(name = "video_view_crdt")  // Logičko ime - biće transformisano!
public class VideoViewCrdt {

    @EmbeddedId
    private VideoViewCrdtId id; // Composite key (video_id + replica_id)

    private Long viewCount;     // Broj pregleda za ovu repliku
    private Long lastUpdated;   // Timestamp poslednje izmene
}
```

**Composite Primary Key:**
```java
@Embeddable
public static class VideoViewCrdtId {
    private Long videoId;      // ID videa (post_id)
    private String replicaId;  // ID replike (npr. "replica_1")
}
```

### 3. VideoViewCrdtService

**Lokacija:** `src/main/java/com/jutjubic/service/VideoViewCrdtService.java`

Ovaj servis upravlja CRDT brojačem.

#### Ključna Metoda: `incrementViewForReplica()`

```java
@Transactional
public void incrementViewForReplica(Long videoId) {
    VideoViewCrdtId id = new VideoViewCrdtId(videoId, replicaId);

    VideoViewCrdt crdt = repository.findById(id)
        .orElse(new VideoViewCrdt(videoId, replicaId));

    // Inkrementuj brojač (grow-only!)
    crdt.setViewCount(crdt.getViewCount() + 1);
    crdt.setLastUpdated(System.currentTimeMillis());

    repository.save(crdt);
}
```

**Šta se dešava:**
1. Pokušava da nađe zapis za `(video_id=1, replica_id="replica_1")`
2. Ako ne postoji, kreira novi sa `view_count=0`
3. Inkrementira `view_count` za 1
4. Čuva u tabelu `video_view_crdt_replica1` (zbog ReplicaPhysicalNamingStrategy)

#### Metoda: `getTotalViewsForVideo()`

```java
public long getTotalViewsForVideo(Long videoId) {
    return repository.findAllByVideoId(videoId)
        .stream()
        .mapToLong(VideoViewCrdt::getViewCount)
        .sum();
}
```

**Šta se dešava:**
1. Pronalazi SVE zapise za dati `video_id` iz **SVOJE tabele** (npr. `video_view_crdt_replica1`)
2. Sabira sve `view_count` vrednosti
3. Vraća ukupan zbir

**VAŽNO:** Ova metoda čita samo iz sopstvene tabele replike! Za punu sinhronizaciju, mora da čita i iz drugih tabela.

## Kako Tečaju Podaci

### Scenario 1: Korisnik Gleda Video na Replici 1

```
1. Korisnik → GET http://localhost:8081/api/posts/1/video

2. PostController.getVideo(postId=1):
   - Inkrementira stari brojač: postService.incrementViewCount(1)
   - Inkrementira CRDT brojač: videoViewCrdtService.incrementViewForReplica(1)

3. VideoViewCrdtService.incrementViewForReplica(1):
   - Traži u video_view_crdt_replica1: WHERE video_id=1 AND replica_id='replica_1'
   - Pronalazi zapis ili kreira novi
   - view_count = view_count + 1
   - Čuva u video_view_crdt_replica1

4. Tabela video_view_crdt_replica1:
   | video_id | replica_id | view_count | last_updated    |
   |----------|------------|------------|-----------------|
   | 1        | replica_1  | 1          | 1737545234567   |

5. Vraća video fajl korisniku
```

### Scenario 2: Korisnik Gleda Video na Replici 2

```
1. Korisnik → GET http://localhost:8082/api/posts/1/video

2. PostController.getVideo(postId=1):
   - Inkrementira CRDT brojač: videoViewCrdtService.incrementViewForReplica(1)

3. VideoViewCrdtService.incrementViewForReplica(1):
   - Traži u video_view_crdt_replica2: WHERE video_id=1 AND replica_id='replica_2'
   - Pronalazi zapis ili kreira novi
   - view_count = view_count + 1
   - Čuva u video_view_crdt_replica2

4. Tabela video_view_crdt_replica2:
   | video_id | replica_id | view_count | last_updated    |
   |----------|------------|------------|-----------------|
   | 1        | replica_2  | 1          | 1737545234987   |
```

### Scenario 3: Čitanje CRDT Brojača

```
1. Korisnik → GET http://localhost:8081/api/posts/1/crdt-views

2. PostController.getCrdtViews(postId=1):
   - Poziva: videoViewCrdtService.getTotalViewsForVideo(1)

3. VideoViewCrdtService.getTotalViewsForVideo(1):
   - SELECT * FROM video_view_crdt_replica1 WHERE video_id=1
   - Pronalazi: [(video_id=1, replica_id='replica_1', view_count=5)]
   - Sabira: 5

4. Vraća JSON:
   {
     "videoId": 1,
     "totalViews": 5,
     "replicaViews": [
       {
         "replicaId": "replica_1",
         "viewCount": 5,
         "lastUpdated": 1737545234567
       }
     ]
   }
```

**NAPOMENA:** Trenutno svaka replika čita samo iz svoje tabele! Za pravu sinhronizaciju, mora da čita iz obe.

## G-Counter CRDT Princip

**G-Counter** = **Grow-only Counter** (brojač koji samo raste)

### Svojstva:

1. **Monoton rast:** Brojač može samo da se inkrementira, nikada ne decrementira
2. **Idempotentnost:** Ista operacija primenjena više puta daje isti rezultat
3. **Komutativnost:** Redosled operacija nije bitan
4. **Asocijativnost:** Grupisanje operacija nije bitno

### Implementacija:

```
G-Counter = suma svih lokalnih brojača

video_view_crdt_replica1:
  video_id=1, replica_id='replica_1', view_count=5

video_view_crdt_replica2:
  video_id=1, replica_id='replica_2', view_count=3

Ukupno pregleda za video 1 = 5 + 3 = 8
```

### Prednosti:

✅ **Nema konflikata:** Svaka replika piše samo svoj brojač
✅ **Eventual consistency:** Replike se na kraju sinhronizuju
✅ **Partition tolerance:** Replike mogu raditi i offline
✅ **Jednostavno spajanje:** Samo saberi sve brojače

### Kako Funkcioniše Sinhronizacija:

```
┌─────────────────────────────────────────────────────────┐
│                  Tok Sinhronizacije                      │
└─────────────────────────────────────────────────────────┘

1. Replika 1 prima 5 zahteva za video 1
   → video_view_crdt_replica1: view_count = 5

2. Replika 2 prima 3 zahteva za video 1
   → video_view_crdt_replica2: view_count = 3

3. Korisnik pita Repliku 1: "Koliko ima pregleda?"
   → Replika 1 čita SAMO svoju tabelu → vraća 5 ❌ (netačno!)

4. Za TAČAN rezultat, Replika 1 mora da:
   a) Čita svoju tabelu: video_view_crdt_replica1 → 5
   b) Čita tuđu tabelu: video_view_crdt_replica2 → 3
   c) Sabere: 5 + 3 = 8 ✅

5. Alternativno: Periodična sinhronizacija
   - Svakih 30s, Replika 1 pročita video_view_crdt_replica2
   - Ažurira svoj cache sa novim vrednostima
   - Sledeći API call vraća zbir iz cache-a
```

## Trenutno Stanje vs Idealno Stanje

### Trenutno ❌

```java
// Svaka replika čita SAMO svoju tabelu
public long getTotalViewsForVideo(Long videoId) {
    return repository.findAllByVideoId(videoId)  // Samo iz video_view_crdt_replica1
        .stream()
        .mapToLong(VideoViewCrdt::getViewCount)
        .sum();
}
```

**Problem:** Replika 1 ne vidi preglede iz Replike 2!

### Idealno Rešenje ✅

```java
public long getTotalViewsForVideo(Long videoId) {
    long viewsFromReplica1 = jdbcTemplate.queryForObject(
        "SELECT COALESCE(SUM(view_count), 0) FROM video_view_crdt_replica1 WHERE video_id = ?",
        Long.class, videoId
    );

    long viewsFromReplica2 = jdbcTemplate.queryForObject(
        "SELECT COALESCE(SUM(view_count), 0) FROM video_view_crdt_replica2 WHERE video_id = ?",
        Long.class, videoId
    );

    return viewsFromReplica1 + viewsFromReplica2;
}
```

**Rešenje:** Eksplicitno čita obe tabele i sabira!

## Konfiguracija Replika

### Replika 1 (application-replica1.properties)

```properties
server.port=8081
replica.id=replica_1
replica.table.suffix=replica1

spring.jpa.hibernate.naming.physical-strategy=com.jutjubic.config.ReplicaPhysicalNamingStrategy
```

### Replika 2 (application-replica2.properties)

```properties
server.port=8082
replica.id=replica_2
replica.table.suffix=replica2

spring.jpa.hibernate.naming.physical-strategy=com.jutjubic.config.ReplicaPhysicalNamingStrategy
```

## Česta Pitanja (FAQ)

### Q: Zašto koristiti odvojene tabele?

**A:** Da bi svaka replika mogla da radi nezavisno bez blokiranja druge replike. Eliminišu se race conditions i deadlock-ovi.

### Q: Kako se podaci sinhronizuju između replika?

**A:** Trenutno se NE sinhronizuju automatski. Potrebno je implementirati:
1. Periodični background job koji čita drugu tabelu
2. REST endpoint za manuelnu sinhronizaciju
3. Ili, čitati obe tabele prilikom svakog API poziva (kao u idealnom rešenju)

### Q: Šta ako jedna replika padne?

**A:** Ništa se ne dešava! Druga replika nastavlja da radi normalno. Kada pala replika ponovo krene, automatski će nastaviti sa svojim brojačem.

### Q: Može li doći do konflikta podataka?

**A:** **NE!** G-counter je dizajniran da nikada nema konflikata. Svaka replika piše samo svoj brojač, a ukupan rezultat je zbir svih brojača.

### Q: Zašto je replica_id deo primary key-a?

**A:** Da bi svaka replika mogla da ima svoj zapis za isti video. Tako imamo:
- `(video_id=1, replica_id='replica_1', view_count=5)`
- `(video_id=1, replica_id='replica_2', view_count=3)`

### Q: Da li mogu da dodam Repliku 3?

**A:** Da! Jednostavno kreiraj `application-replica3.properties`:
```properties
server.port=8083
replica.id=replica_3
replica.table.suffix=replica3
```

Pokreni repliku i automatski će se kreirati `video_view_crdt_replica3` tabela.
