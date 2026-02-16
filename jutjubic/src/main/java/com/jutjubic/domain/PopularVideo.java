package com.jutjubic.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "popular_videos")
@Getter
@Setter
public class PopularVideo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pipeline_run_at", nullable = false)
    private Instant pipelineRunAt;

    @Column(name = "video_1_id")
    private Long video1Id;

    @Column(name = "video_1_score")
    private Long video1Score;

    @Column(name = "video_2_id")
    private Long video2Id;

    @Column(name = "video_2_score")
    private Long video2Score;

    @Column(name = "video_3_id")
    private Long video3Id;

    @Column(name = "video_3_score")
    private Long video3Score;
}
