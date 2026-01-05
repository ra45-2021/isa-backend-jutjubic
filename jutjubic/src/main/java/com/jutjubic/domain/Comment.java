package com.jutjubic.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
        name = "comments",
        indexes = {
                @Index(name = "idx_comment_post_created", columnList = "post_id,created_at"),
                @Index(name = "idx_comment_author_created", columnList = "author_id,created_at")
        }
)
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false, length = 2000)
    private String text;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected Comment() {
    }

    public Comment(Post post, User author, String text) {
        this.post = post;
        this.author = author;
        this.text = text;
    }

    public Long getId() { return id; }

    public Post getPost() { return post; }
    public void setPost(Post post) { this.post = post; }

    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
