package com.jutjubic.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "post_likes")
public class PostLike {

    @EmbeddedId
    @Getter @Setter
    private PostLikeId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("postId")
    @JoinColumn(name = "post_id")
    @Getter @Setter
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    @Getter @Setter
    private User user;

    public PostLike() {}

    public PostLike(Post post, User user) {
        this.post = post;
        this.user = user;
        this.id = new PostLikeId(post.getId(), user.getId());
    }
}
