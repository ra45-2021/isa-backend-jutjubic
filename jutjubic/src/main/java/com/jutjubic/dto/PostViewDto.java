package com.jutjubic.dto;

import java.time.Instant;
import java.util.List;

public class PostViewDto {

    private Long id;
    private String title;
    private String description;
    private List<String> tags;
    private String videoUrl;
    private String thumbnailUrl;
    private Instant createdAt;
    private UserDto author;


    public PostViewDto(
            Long id,
            String title,
            String description,
            List<String> tags,
            String videoUrl,
            String thumbnailUrl,
            Instant createdAt,
            UserDto author
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.tags = tags;
        this.videoUrl = videoUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.createdAt = createdAt;
        this.author = author;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public List<String> getTags() { return tags; }
    public String getVideoUrl() { return videoUrl; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public Instant getCreatedAt() { return createdAt; }
    public UserDto getAuthor() { return author; }
}
