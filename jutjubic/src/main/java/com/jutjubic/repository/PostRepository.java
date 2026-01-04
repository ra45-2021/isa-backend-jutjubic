package com.jutjubic.repository;

import com.jutjubic.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("""
        SELECT p
        FROM Post p
        JOIN FETCH p.author
        ORDER BY p.createdAt DESC
    """)
    List<Post> findAllWithAuthorNewestFirst();
}
