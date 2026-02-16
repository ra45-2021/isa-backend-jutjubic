package com.jutjubic.repository;

import com.jutjubic.domain.PopularVideo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PopularVideoRepository extends JpaRepository<PopularVideo, Long> {

    Optional<PopularVideo> findTopByOrderByPipelineRunAtDesc();
}
