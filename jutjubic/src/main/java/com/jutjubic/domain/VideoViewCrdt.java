package com.jutjubic.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Entity klasa za CRDT (Conflict-free Replicated Data Type) brojač pregleda videa.
 * Svaka replika čuva informaciju o broju pregleda u svojoj tabeli.
 *
 * Struktura podržava G-counter (Grow-only counter) pattern gde svaka replika
 * prati svoj lokalni brojač i komunicira sa drugim replikama za sinhronizaciju.
 *
 * NAPOMENA: Ime tabele se dinamički postavlja kroz ReplicaTableNameConfig na osnovu
 * replica.table.suffix property-ja (npr. video_view_crdt_replica1, video_view_crdt_replica2).
 */
@Entity
@Table(name = "video_view_crdt")
@Getter
@Setter
public class VideoViewCrdt {

    @EmbeddedId
    private VideoViewCrdtId id;

    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;

    @Column(name = "last_updated")
    private Long lastUpdated;

    public VideoViewCrdt() {
    }

    public VideoViewCrdt(Long videoId, String replicaId) {
        this.id = new VideoViewCrdtId(videoId, replicaId);
        this.viewCount = 0L;
        this.lastUpdated = System.currentTimeMillis();
    }

    @Embeddable
    @Getter
    @Setter
    public static class VideoViewCrdtId implements Serializable {

        @Column(name = "video_id", nullable = false)
        private Long videoId;

        @Column(name = "replica_id", nullable = false, length = 50)
        private String replicaId;

        public VideoViewCrdtId() {
        }

        public VideoViewCrdtId(Long videoId, String replicaId) {
            this.videoId = videoId;
            this.replicaId = replicaId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof VideoViewCrdtId)) return false;
            VideoViewCrdtId that = (VideoViewCrdtId) o;
            return videoId.equals(that.videoId) && replicaId.equals(that.replicaId);
        }

        @Override
        public int hashCode() {
            return 31 * videoId.hashCode() + replicaId.hashCode();
        }
    }
}
