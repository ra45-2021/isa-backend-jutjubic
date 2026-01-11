package com.jutjubic.dto;

import java.time.Instant;

public class CommentViewDto {

    private Long id;
    private String authorUsername;
    private String authorProfileImageUrl;
    private Instant createdAt;
    private String text;

    public CommentViewDto(
            Long id,
            String authorUsername,
            String authorProfileImageUrl,
            Instant createdAt,
            String text
    ) {
        this.id = id;
        this.authorUsername = authorUsername;
        this.authorProfileImageUrl = authorProfileImageUrl;
        this.createdAt = createdAt;
        this.text = text;
    }

    public Long getId() { return id; }
    public String getAuthorUsername() { return authorUsername; }
    public String getAuthorProfileImageUrl() {return authorProfileImageUrl;}
    public Instant getCreatedAt() { return createdAt; }
    public String getText() { return text; }
}
