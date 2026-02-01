package com.jutjubic.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "transcoding_jobs",
        uniqueConstraints = @UniqueConstraint(name="uk_transcoding_job_jobid", columnNames="jobId"))
@Getter @Setter
public class TranscodingJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private String jobId;

    @Column(nullable = false, updatable = false)
    private Long postId;

    @Column(nullable = false)
    private String status; // RECEIVED, PROCESSING, DONE, FAILED

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @Column
    private Instant finishedAt;

    @Column(columnDefinition = "text")
    private String error;
}
