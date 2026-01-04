package com.jutjubic.dto;

import java.time.Instant;

public class PostViewDto {

    private Long id;
    private String title;
    private String description;
    private String videoUrl;
    private String thumbnailUrl;
    private Instant createdAt;
    private UserDto author;

    public PostViewDto(
            Long id,
            String title,
            String description,
            String videoUrl,
            String thumbnailUrl,
            Instant createdAt,
            UserDto author
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.videoUrl = videoUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.createdAt = createdAt;
        this.author = author;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public UserDto getAuthor() {
        return author;
    }
}
