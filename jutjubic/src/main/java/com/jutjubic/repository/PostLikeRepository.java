package com.jutjubic.repository;


import com.jutjubic.domain.PostLike;
import com.jutjubic.domain.PostLikeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, PostLikeId> {

    boolean existsById(PostLikeId id);

    long countByPostId(Long postId);

    Optional<PostLike> findById(PostLikeId id);
}
