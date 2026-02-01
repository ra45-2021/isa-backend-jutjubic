package com.jutjubic.repository;

import com.jutjubic.domain.TranscodingJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TranscodingJobRepository extends JpaRepository<TranscodingJob, Long> {
    boolean existsByJobId(String jobId);
    Optional<TranscodingJob> findByJobId(String jobId);
}
