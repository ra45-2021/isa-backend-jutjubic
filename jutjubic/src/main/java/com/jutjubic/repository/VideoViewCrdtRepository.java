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

@Repository
public interface VideoViewCrdtRepository extends JpaRepository<VideoViewCrdt, VideoViewCrdt.VideoViewCrdtId> {

    @Query("SELECT v FROM VideoViewCrdt v WHERE v.id.videoId = :videoId")
    List<VideoViewCrdt> findAllByVideoId(@Param("videoId") Long videoId);

    @Query("SELECT v FROM VideoViewCrdt v WHERE v.id.videoId = :videoId AND v.id.replicaId = :replicaId")
    Optional<VideoViewCrdt> findByVideoIdAndReplicaId(@Param("videoId") Long videoId, @Param("replicaId") String replicaId);

    @Modifying
    @Transactional
    @Query("UPDATE VideoViewCrdt v SET v.viewCount = v.viewCount + 1, v.lastUpdated = :timestamp " +
            "WHERE v.id.videoId = :videoId AND v.id.replicaId = :replicaId")
    void incrementViewCount(@Param("videoId") Long videoId,
                            @Param("replicaId") String replicaId,
                            @Param("timestamp") Long timestamp);

    @Modifying
    @Transactional
    @Query("UPDATE VideoViewCrdt v SET v.viewCount = :count, v.lastUpdated = :timestamp " +
            "WHERE v.id.videoId = :videoId AND v.id.replicaId = :replicaId")
    void updateViewCount(@Param("videoId") Long videoId,
                        @Param("replicaId") String replicaId,
                        @Param("count") Long count,
                        @Param("timestamp") Long timestamp);


    @Query("SELECT COALESCE(SUM(v.viewCount), 0) FROM VideoViewCrdt v WHERE v.id.videoId = :videoId")
    Long getTotalViewCount(@Param("videoId") Long videoId);

    @Query("SELECT COUNT(v) > 0 FROM VideoViewCrdt v WHERE v.id.videoId = :videoId AND v.id.replicaId = :replicaId")
    boolean existsByVideoIdAndReplicaId(@Param("videoId") Long videoId, @Param("replicaId") String replicaId);

    @Query("SELECT v FROM VideoViewCrdt v WHERE v.id.replicaId = :replicaId")
    List<VideoViewCrdt> findAllByReplicaId(@Param("replicaId") String replicaId);
}
