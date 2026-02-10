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
    private Long commentCount;
    private Long likeCount;
    private Boolean likedByMe;
    private Long view_count;


    public PostViewDto(
            Long id,
            String title,
            String description,
            List<String> tags,
            String videoUrl,
            String thumbnailUrl,
            Instant createdAt,
            UserDto author,
            Long commentCount,
            Long likeCount,
            Boolean likedByMe,
            Long view_count
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.tags = tags;
        this.videoUrl = videoUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.createdAt = createdAt;
        this.author = author;
        this.commentCount = commentCount;
        this.likeCount = likeCount;
        this.likedByMe = likedByMe;
        this.view_count = view_count;
    }

    public PostViewDto(
            Long id,
            String title,
            String description,
            String tagsText,
            String videoUrl,
            String thumbnailUrl,
            Instant createdAt,

            Long authorId,
            String authorUsername,
            String authorName,
            String authorSurname,
            String authorProfileImageUrl,

            Long commentCount,
            Long likeCount,
            Boolean likedByMe,
            Long view_count
    ) {
        this.id = id;
        this.title = title;
        this.description = description;

        if (tagsText == null || tagsText.isBlank()) {
            this.tags = List.of();
        } else {
            this.tags = java.util.Arrays.stream(tagsText.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .toList();
        }

        this.videoUrl = videoUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.createdAt = createdAt;

        this.author = new UserDto(
                authorId,
                authorUsername,
                authorName,
                authorSurname,
                authorProfileImageUrl
        );

        this.commentCount = commentCount;
        this.likeCount = likeCount;
        this.likedByMe = likedByMe;
        this.view_count = view_count;
    }



    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public List<String> getTags() { return tags; }
    public String getVideoUrl() { return videoUrl; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public Instant getCreatedAt() { return createdAt; }
    public UserDto getAuthor() { return author; }
    public Long getCommentCount() { return commentCount; }
    public Long getLikeCount() { return likeCount; }
    public Boolean getLikedByMe() { return likedByMe; }
    public Long getViewCount() { return view_count; }
    public void setViewCount(Long viewCount) {this.view_count = viewCount;}

}
