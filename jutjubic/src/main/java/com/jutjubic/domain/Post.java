package com.jutjubic.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
        name = "posts",
        indexes = {
                @Index(name = "idx_post_created_at", columnList = "created_at"),
                @Index(name = "idx_post_author_created", columnList = "author_id,created_at")
        }
)
public class Post {

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Getter
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Setter
    @Getter
    @Column(nullable = false, length = 150)
    private String title;

    @Setter
    @Getter
    @Column(columnDefinition = "text")
    private String description;

    @Setter
    @Getter
    @Column(name = "tags_text", columnDefinition = "text")
    private String tags;

    @Setter
    @Getter
    @Column(name = "video_url", nullable = false)
    private String videoUrl;

    @Setter
    @Getter
    @Column(name = "thumbnail_url", nullable = false)
    private String thumbnailUrl;

    @Setter
    @Getter
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Setter
    @Getter
    @Column(name = "location_lat")
    private Double locationLat;
    @Setter
    @Getter
    @Column(name = "location_lon")
    private Double locationLon;

    @Setter
    @Getter
    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;

    public Post() {
    }

    public Post(User author, String title, String videoUrl) {
        this.author = author;
        this.title = title;
        this.videoUrl = videoUrl;
    }
}
