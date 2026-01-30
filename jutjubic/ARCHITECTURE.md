# 🏗️ Arhitektura CRDT sistema sa replikama

## 📊 Pregled komponenti

### 1. **Replike aplikacije**
- **Replika 1**: Port 8081, replica_id = "replica_1"
- **Replika 2**: Port 8082, replica_id = "replica_2"
- **Replika N**: Port 808N, replica_id = "replica_N"

### 2. **Baza podataka**
- **PostgreSQL**: Port 5432
- **Baza**: jutjubic
- **Deljeno između svih replika**

### 3. **CRDT tabela**: `video_view_crdt`

---

## 🗄️ Struktura podataka

### Tabela: `video_view_crdt`

```sql
CREATE TABLE video_view_crdt (
    video_id BIGINT NOT NULL,        -- ID videa (FK ka posts tabeli)
    replica_id VARCHAR(50) NOT NULL, -- ID replike ("replica_1", "replica_2", ...)
    view_count BIGINT NOT NULL,      -- Broj pregleda koje je ova replika registrovala
    last_updated BIGINT,             -- Timestamp poslednje izmene (milisekunde)
    PRIMARY KEY (video_id, replica_id)
);
```

### Primer podataka:

```
╔══════════╦════════════╦═══════════╦═════════════════╗
║ video_id ║ replica_id ║ view_count║  last_updated   ║
╠══════════╬════════════╬═══════════╬═════════════════╣
║    1     ║ replica_1  ║    25     ║ 1705234567890   ║
║    1     ║ replica_2  ║    23     ║ 1705234567891   ║
║    2     ║ replica_1  ║    10     ║ 1705234567892   ║
║    2     ║ replica_2  ║    12     ║ 1705234567893   ║
╚══════════╩════════════╩═══════════╩═════════════════╝

Ukupno pregleda za video 1: 25 + 23 = 48
Ukupno pregleda za video 2: 10 + 12 = 22
```

---

## 🔄 Flow dijagram: Povećanje brojača

```
┌─────────┐
│ Korisnik│
└────┬────┘
     │
     │ GET /api/posts/1/video
     ▼
┌────────────────┐
│ PostController │
└────┬───────────┘
     │
     │ incrementViewForReplica(1)
     ▼
┌──────────────────────┐
│ VideoViewCrdtService │
└──────┬───────────────┘
       │
       │ 1. Proveri da li postoji red (video_id=1, replica_id=replica_1)
       │ 2. Ako ne postoji → INSERT (view_count=1)
       │ 3. Ako postoji → UPDATE view_count = view_count + 1
       ▼
┌────────────────────────┐
│ VideoViewCrdtRepository│
└──────┬─────────────────┘
       │
       │ SQL UPDATE
       ▼
┌──────────────────┐
│   PostgreSQL     │
│ video_view_crdt  │
└──────────────────┘
```

---

## 📖 Flow dijagram: Čitanje ukupnog broja pregleda

```
┌─────────┐
│ Korisnik│
└────┬────┘
     │
     │ GET /api/posts/1/crdt-views
     ▼
┌────────────────┐
│ PostController │
└────┬───────────┘
     │
     │ getTotalViewCount(1)
     ▼
┌──────────────────────┐
│ VideoViewCrdtService │
└──────┬───────────────┘
       │
       │ SELECT SUM(view_count) FROM video_view_crdt WHERE video_id = 1
       ▼
┌────────────────────────┐
│ VideoViewCrdtRepository│
└──────┬─────────────────┘
       │
       │ SQL Query
       ▼
┌──────────────────┐
│   PostgreSQL     │
│ video_view_crdt  │
│ ┌──────────────┐ │
│ │ video_id=1   │ │
│ │ replica_1: 25│ │
│ │ replica_2: 23│ │
│ └──────────────┘ │
└──────────────────┘
       │
       │ Rezultat: SUM = 48
       ▼
┌─────────┐
│ Response│
│ {       │
│  total: │
│    48   │
│ }       │
└─────────┘
```

---

## 🔄 G-counter operacije

### **Increment** (Grow-only)

```java
// Kada korisnik pogleda video na Replici 1:
public void incrementViewForReplica(Long videoId) {
    // Povećaj SAMO svoj brojač
    UPDATE video_view_crdt
    SET view_count = view_count + 1
    WHERE video_id = ? AND replica_id = 'replica_1'
}
```

**Važno**: Replika 1 **NIKAD** ne dira brojač za `replica_2`!

### **Read** (Zbir svih brojača)

```java
public Long getTotalViewCount(Long videoId) {
    // Saberi SVE brojače
    SELECT SUM(view_count)
    FROM video_view_crdt
    WHERE video_id = ?
}
```

### **Merge** (TODO - za kolegu)

```java
// Kada Replika 2 primi poruku od Replike 1:
// Poruka: { videoId: 1, replicaId: "replica_1", count: 30 }

public void mergeFromOtherReplica(Long videoId, String sourceReplicaId, Long receivedCount) {
    // 1. Pročitaj lokalni brojač za (video_id=1, replica_id="replica_1")
    Long localCount = repository.findByVideoIdAndReplicaId(videoId, sourceReplicaId)
                                 .map(VideoViewCrdt::getViewCount)
                                 .orElse(0L);

    // 2. Uzmi MAXIMUM (G-counter princip)
    Long mergedCount = Math.max(localCount, receivedCount);

    // 3. Ažuriraj lokalnu tabelu
    UPDATE video_view_crdt
    SET view_count = mergedCount
    WHERE video_id = ? AND replica_id = ?
}
```

**Zašto MAX?** Jer G-counter samo raste! Ako ja imam 25, a ti mi šalješ 30, znači da si ti video 5 novih inkremenata koje ja nisam video.

---

## 🔀 Scenario 1: Bez sinhronizacije

```
T=0: Video 1 ima 0 pregleda

T=1: Korisnik1 → Load Balancer → Replika 1 → increment
     Replika 1 tabela: { replica_1: 1, replica_2: 0 } → Total: 1

T=2: Korisnik2 → Load Balancer → Replika 2 → increment
     Replika 2 tabela: { replica_1: 0, replica_2: 1 } → Total: 1

❌ Problem: Replike nisu konzistentne!
   - Replika 1 misli da ima 1 pregled
   - Replika 2 misli da ima 1 pregled
   - Ali ukupno ima 2 pregleda!
```

---

## ✅ Scenario 2: Sa sinhronizacijom (TODO)

```
T=0: Video 1 ima 0 pregleda

T=1: Korisnik1 → Replika 1 → increment
     Replika 1 tabela: { replica_1: 1, replica_2: 0 } → Total: 1
     Replika 1 šalje poruku → MQ: { videoId: 1, replicaId: "replica_1", count: 1 }

T=2: Korisnik2 → Replika 2 → increment
     Replika 2 tabela: { replica_1: 0, replica_2: 1 } → Total: 1
     Replika 2 šalje poruku → MQ: { videoId: 1, replicaId: "replica_2", count: 1 }

T=3: SYNC!
     Replika 1 prima poruku od Replike 2:
       - Merge: MAX(0, 1) = 1
       - Replika 1 tabela: { replica_1: 1, replica_2: 1 } → Total: 2 ✅

     Replika 2 prima poruku od Replike 1:
       - Merge: MAX(0, 1) = 1
       - Replika 2 tabela: { replica_1: 1, replica_2: 1 } → Total: 2 ✅

✅ Rezultat: Obe replike su konzistentne!
```

---

## 🧩 Java komponente

### 1. **Entity**: `VideoViewCrdt.java`
```
┌─────────────────────────────┐
│     VideoViewCrdt           │
├─────────────────────────────┤
│ - id: VideoViewCrdtId       │ (composite key)
│   - videoId: Long           │
│   - replicaId: String       │
│ - viewCount: Long           │
│ - lastUpdated: Long         │
└─────────────────────────────┘
```

### 2. **Repository**: `VideoViewCrdtRepository.java`
```
┌──────────────────────────────────────┐
│   VideoViewCrdtRepository            │
├──────────────────────────────────────┤
│ + findAllByVideoId(videoId)          │
│ + findByVideoIdAndReplicaId(...)     │
│ + incrementViewCount(...)            │
│ + updateViewCount(...)               │
│ + getTotalViewCount(videoId)         │
│ + existsByVideoIdAndReplicaId(...)   │
└──────────────────────────────────────┘
```

### 3. **Service**: `VideoViewCrdtService.java`
```
┌──────────────────────────────────────┐
│     VideoViewCrdtService             │
├──────────────────────────────────────┤
│ - replicaId: String                  │ (@Value)
│ - repository: Repository             │
├──────────────────────────────────────┤
│ + incrementViewForReplica(videoId)   │ ✅ Implementirano
│ + getTotalViewCount(videoId)         │ ✅ Implementirano
│ + getAllCountersForVideo(videoId)    │ ✅ Implementirano
│ + mergeFromOtherReplica(...)         │ ⏳ TODO (kolega)
│ - broadcastToOtherReplicas(...)      │ ⏳ TODO (kolega)
└──────────────────────────────────────┘
```

### 4. **Controller**: `PostController.java`
```
┌──────────────────────────────────────┐
│       PostController                 │
├──────────────────────────────────────┤
│ + getVideo(postId)                   │ → poziva incrementViewForReplica()
│ + getCrdtViews(postId)               │ → vraća CRDT brojače
└──────────────────────────────────────┘
```

---

## 📡 Sync mehanizam (TODO - sledeća faza)

### Opcija A: Periodični sync (preporučeno za početak)

```
┌─────────────┐                    ┌─────────────┐
│  Replika 1  │                    │  Replika 2  │
└──────┬──────┘                    └──────┬──────┘
       │                                  │
       │ @Scheduled(fixedRate = 5000)    │
       │                                  │
       │ 1. Učitaj sve svoje brojače     │
       │ 2. Pošalji poruke u MQ          │
       ▼                                  ▼
    ┌──────────────────────────────────┐
    │      Message Queue (RabbitMQ)    │
    └──────────────────────────────────┘
       │                                  │
       │ 3. Primi poruke od drugih       │
       │ 4. Pozovi merge() za svaku      │
       ▼                                  ▼
```

### Opcija B: Event-driven sync

```
Korisnik pogleda video → increment() → publish(poruka) → MQ → druge replike → merge()
```

---

## 🎯 Svojstva G-counter algoritma

### ✅ **Commutative** (redosled nije bitan)
```
merge(A, merge(B, C)) = merge(B, merge(A, C))
```

### ✅ **Idempotent** (dupla poruka ne pravi problem)
```
merge(A, A) = A
```

### ✅ **Monotonic** (samo raste)
```
merge(A, B) >= A  i  merge(A, B) >= B
```

### ✅ **Eventually consistent**
```
Nakon dovoljno vremena, sve replike konvergiraju ka istom stanju
```

---

## 🚀 Sledeći koraci

1. **Implementirati merge logiku** u `VideoViewCrdtService`
2. **Dodati Message Queue** (RabbitMQ ili in-memory)
3. **Implementirati periodični sync** (`@Scheduled`)
4. **Dodati Load Balancer** (Nginx)
5. **Testirati konzistentnost** sa 100+ konkurentnih zahteva

---

**Autor:** Claude Code
**Datum:** 2026-01-21
