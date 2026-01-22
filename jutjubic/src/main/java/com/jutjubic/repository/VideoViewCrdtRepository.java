package com.jutjubic.repository;

import com.jutjubic.domain.VideoViewCrdt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Repository za rad sa CRDT brojačima pregleda videa.
 * Svaka replika koristi ovaj repository za pristup svojoj tabeli.
 */
@Repository
public interface VideoViewCrdtRepository extends JpaRepository<VideoViewCrdt, VideoViewCrdt.VideoViewCrdtId> {

    /**
     * Pronalazi sve brojače za dati video (sve replike).
     *
     * @param videoId ID videa
     * @return Lista brojača za sve replike
     */
    @Query("SELECT v FROM VideoViewCrdt v WHERE v.id.videoId = :videoId")
    List<VideoViewCrdt> findAllByVideoId(@Param("videoId") Long videoId);

    /**
     * Pronalazi brojač za specifičan video i repliku.
     *
     * @param videoId ID videa
     * @param replicaId ID replike
     * @return Optional sa brojačem ako postoji
     */
    @Query("SELECT v FROM VideoViewCrdt v WHERE v.id.videoId = :videoId AND v.id.replicaId = :replicaId")
    Optional<VideoViewCrdt> findByVideoIdAndReplicaId(@Param("videoId") Long videoId, @Param("replicaId") String replicaId);

    /**
     * Povećava brojač pregleda za datu repliku.
     * Ova metoda će biti pozvana kada korisnik pogleda video na ovoj replici.
     *
     * @param videoId ID videa
     * @param replicaId ID replike koja registruje pregled
     */
    @Modifying
    @Transactional
    @Query("UPDATE VideoViewCrdt v SET v.viewCount = v.viewCount + 1, v.lastUpdated = :timestamp " +
            "WHERE v.id.videoId = :videoId AND v.id.replicaId = :replicaId")
    void incrementViewCount(@Param("videoId") Long videoId,
                            @Param("replicaId") String replicaId,
                            @Param("timestamp") Long timestamp);

    /**
     * Ažurira brojač za specifičnu repliku (koristi se u merge operaciji).
     * Postavlja tačnu vrednost brojača primljenu od druge replike.
     *
     * @param videoId ID videa
     * @param replicaId ID replike
     * @param count Nova vrednost brojača
     * @param timestamp Timestamp ažuriranja
     */
    @Modifying
    @Transactional
    @Query("UPDATE VideoViewCrdt v SET v.viewCount = :count, v.lastUpdated = :timestamp " +
            "WHERE v.id.videoId = :videoId AND v.id.replicaId = :replicaId")
    void updateViewCount(@Param("videoId") Long videoId,
                        @Param("replicaId") String replicaId,
                        @Param("count") Long count,
                        @Param("timestamp") Long timestamp);

    /**
     * Računa ukupan broj pregleda za dati video sabiranjem svih brojača.
     * Ovo je implementacija G-counter read operacije.
     *
     * @param videoId ID videa
     * @return Ukupan broj pregleda
     */
    @Query("SELECT COALESCE(SUM(v.viewCount), 0) FROM VideoViewCrdt v WHERE v.id.videoId = :videoId")
    Long getTotalViewCount(@Param("videoId") Long videoId);

    /**
     * Proverava da li postoji unos za dati video i repliku.
     *
     * @param videoId ID videa
     * @param replicaId ID replike
     * @return true ako postoji, false inače
     */
    @Query("SELECT COUNT(v) > 0 FROM VideoViewCrdt v WHERE v.id.videoId = :videoId AND v.id.replicaId = :replicaId")
    boolean existsByVideoIdAndReplicaId(@Param("videoId") Long videoId, @Param("replicaId") String replicaId);
}
