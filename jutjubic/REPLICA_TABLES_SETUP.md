# Konfiguracija Odvojenih Tabela za Replike

## Pregled

Svaka replika koristi **istu PostgreSQL bazu** (`jutjubic`), ali **odvojene tabele** za CRDT brojač pregleda:

- **Replika 1** (port 8081): koristi tabelu `video_view_crdt_replica1`
- **Replika 2** (port 8082): koristi tabelu `video_view_crdt_replica2`

Sve ostale tabele (`users`, `posts`, `comments`, itd.) su **zajedničke** za obe replike.

## Implementirane Izmene

### 1. PhysicalNamingStrategy

Kreirana je `ReplicaPhysicalNamingStrategy` klasa koja automatski dodaje sufiks **samo** na `video_view_crdt` tabelu:

```java
// src/main/java/com/jutjubic/config/ReplicaPhysicalNamingStrategy.java

public class ReplicaPhysicalNamingStrategy extends PhysicalNamingStrategyStandardImpl {

    @Override
    public Identifier toPhysicalTableName(Identifier logicalName, JdbcEnvironment context) {
        String tableName = logicalName.getText();

        // Dodaj sufiks SAMO za video_view_crdt tabelu
        if ("video_view_crdt".equals(tableName)) {
            return Identifier.toIdentifier(tableName + "_" + tableSuffix);
        }

        // Sve ostale tabele ostaju neizmenjene
        return super.toPhysicalTableName(logicalName, context);
    }
}
```

### 2. Konfiguracija Replika

#### application-replica1.properties
```properties
server.port=8081
replica.id=replica_1
replica.table.suffix=replica1

spring.jpa.hibernate.naming.physical-strategy=com.jutjubic.config.ReplicaPhysicalNamingStrategy
```

#### application-replica2.properties
```properties
server.port=8082
replica.id=replica_2
replica.table.suffix=replica2

spring.jpa.hibernate.naming.physical-strategy=com.jutjubic.config.ReplicaPhysicalNamingStrategy
```

### 3. VideoViewCrdt Entity

```java
@Entity
@Table(name = "video_view_crdt")
public class VideoViewCrdt {
    // Hibernate će automatski transformisati ovo ime na osnovu ReplicaPhysicalNamingStrategy
    // replica1 -> video_view_crdt_replica1
    // replica2 -> video_view_crdt_replica2
}
```

## Struktura Baze Podataka

Nakon pokretanja obe replike, baza `jutjubic` će sadržati:

### Zajedničke Tabele (koriste obe replike):
- `users`
- `posts`
- `comments`
- `post_likes`
- `subscriptions`

### Odvojene CRDT Tabele:
- `video_view_crdt_replica1` - koristi samo Replika 1
- `video_view_crdt_replica2` - koristi samo Replika 2

## Pokretanje Replika

### Pokretanje Replike 1:
```bash
# Windows
start-replica1.bat

# Linux/Mac
./start-replica1.sh

# Maven direktno
mvn spring-boot:run -Dspring-boot.run.profiles=replica1
```

### Pokretanje Replike 2:
```bash
# Windows
start-replica2.bat

# Linux/Mac
./start-replica2.sh

# Maven direktno
mvn spring-boot:run -Dspring-boot.run.profiles=replica2
```

## Testiranje

### 1. Provera da li Replika 1 koristi svoju tabelu:
```bash
# Pogleda video na replici 1
curl http://localhost:8081/api/posts/1/video

# Proveri CRDT brojač
curl http://localhost:8081/api/posts/1/crdt-views
```

### 2. Provera PostgreSQL baze:
```sql
-- Proveri tabele u bazi
SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'public'
ORDER BY table_name;

-- Trebalo bi da vidiš:
-- video_view_crdt_replica1
-- video_view_crdt_replica2

-- Proveri sadržaj tabele replike 1
SELECT * FROM video_view_crdt_replica1;

-- Proveri sadržaj tabele replike 2
SELECT * FROM video_view_crdt_replica2;
```

### 3. Provera da li Replika 2 koristi svoju tabelu:
```bash
# Pogleda video na replici 2
curl http://localhost:8082/api/posts/1/video

# Proveri CRDT brojač
curl http://localhost:8082/api/posts/1/crdt-views
```

## Kako Radi Sinhronizacija

1. **Kada korisnik gleda video na Replici 1:**
   - Inkrementira se brojač u `video_view_crdt_replica1`
   - `replica_id` = `replica_1`, `view_count` += 1

2. **Kada korisnik gleda video na Replici 2:**
   - Inkrementira se brojač u `video_view_crdt_replica2`
   - `replica_id` = `replica_2`, `view_count` += 1

3. **Za ukupan broj pregleda:**
   - Endpoint `/api/posts/{postId}/crdt-views` čita **obe tabele**
   - Sabira `view_count` iz `video_view_crdt_replica1` i `video_view_crdt_replica2`
   - Vraća ukupan zbir (G-counter princip)

## Implementacija Sinhronizacije (TODO)

Za punu funkcionalnost potrebno je implementirati:

1. **Periodična sinhronizacija:** Svaka replika periodično čita drugu tabelu
2. **API endpoint za sync:** `/api/sync/crdt-views` koji manuelno pokreće sinhronizaciju
3. **Merge logika:** Kada replika pročita drugu tabelu, ažurira svoj lokalni cache

Primer:
```java
@Scheduled(fixedDelay = 30000) // Svakih 30 sekundi
public void syncWithOtherReplicas() {
    // Pročitaj video_view_crdt_replica2 (ako si replica1)
    // Ažuriraj cache sa novim vrednostima
}
```

## Prednosti Ovog Pristupa

✅ Jednostavno za debagovanje - svaka replika ima jasno odvojene podatke
✅ Nema potrebe za dve odvojene baze
✅ Sve ostale tabele su zajedničke (users, posts, itd.)
✅ Lako se dodaju nove replike - samo novi `replica.table.suffix`
✅ CRDT G-counter princip - samo sabiranje, nema konflikata

## Napomene

- **DDL auto mode:** Trenutno je postavljen `create-drop`, što znači da se tabele brišu pri svakom restartovanju. Za produkciju koristiti `update` ili `validate`.
- **Inicijalni podaci:** `data.sql` se izvršava pri svakom pokretanju. Razmotriti Flyway/Liquibase migracije za produkciju.
