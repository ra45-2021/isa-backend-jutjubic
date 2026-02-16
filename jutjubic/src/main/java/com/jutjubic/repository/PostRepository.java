package com.jutjubic.repository;

import com.jutjubic.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import com.jutjubic.dto.PostViewDto;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("""
  SELECT new com.jutjubic.dto.PostViewDto(
      p.id,
      p.title,
      p.description,
      p.tags,
      p.videoUrl,
      CONCAT(CONCAT('/api/posts/', p.id), '/thumbnail'),
      p.createdAt,

      a.id,
      a.username,
      a.name,
      a.surname,
      a.profileImageUrl,

      (SELECT COUNT(c) FROM Comment c WHERE c.post = p),
      (SELECT COUNT(l) FROM PostLike l WHERE l.post = p),
      (SELECT COUNT(l) > 0 FROM PostLike l WHERE l.post = p AND l.user.username = :currentUsername),
      p.viewCount,
      p.scheduledAt,
      p.durationSeconds
  )
  FROM Post p
  JOIN p.author a
  LEFT JOIN Comment c ON c.post = p
  WHERE p.scheduledAt IS NULL OR p.scheduledAt <= CURRENT_TIMESTAMP
  GROUP BY
      p.id, p.title, p.description, p.tags, p.videoUrl, p.createdAt,
      a.id, a.username, a.name, a.surname, a.profileImageUrl,
      p.durationSeconds
  ORDER BY p.createdAt DESC
""")
    List<PostViewDto> findAllPostViewsNewestFirst(@Param("currentUsername") String currentUsername);

    @Query("""
  SELECT new com.jutjubic.dto.PostViewDto(
      p.id,
      p.title,
      p.description,
      p.tags,
      p.videoUrl,
      CONCAT(CONCAT('/api/posts/', p.id), '/thumbnail'),
      p.createdAt,

      a.id,
      a.username,
      a.name,
      a.surname,
      a.profileImageUrl,

      (SELECT COUNT(c) FROM Comment c WHERE c.post = p),
      (SELECT COUNT(l) FROM PostLike l WHERE l.post = p),
      (SELECT COUNT(l) > 0 FROM PostLike l WHERE l.post = p AND l.user.username = :currentUsername),
      p.viewCount,
      p.scheduledAt,
      p.durationSeconds
  )
  FROM Post p
  JOIN p.author a
  LEFT JOIN Comment c ON c.post = p
  WHERE a.username = :username AND (p.scheduledAt IS NULL OR p.scheduledAt <= CURRENT_TIMESTAMP)
  GROUP BY
      p.id, p.title, p.description, p.tags, p.videoUrl, p.createdAt,
      a.id, a.username, a.name, a.surname, a.profileImageUrl,
      p.durationSeconds
  ORDER BY p.createdAt DESC
""")
    List<PostViewDto> findAllPostViewsByUsernameNewestFirst(
            @Param("username") String username,
            @Param("currentUsername") String currentUsername
    );

    @Query("""
  SELECT new com.jutjubic.dto.PostViewDto(
      p.id, p.title, p.description, p.tags, p.videoUrl,
      CONCAT(CONCAT('/api/posts/', p.id), '/thumbnail'),
      p.createdAt,
      a.id, a.username, a.name, a.surname, a.profileImageUrl,
      (SELECT COUNT(c) FROM Comment c WHERE c.post = p),
      (SELECT COUNT(l) FROM PostLike l WHERE l.post = p),
      (SELECT COUNT(l) > 0 FROM PostLike l WHERE l.post = p AND l.user.username = :currentUsername),
      p.viewCount,
      p.scheduledAt,
      p.durationSeconds
  )
  FROM Post p
  JOIN p.author a
  LEFT JOIN Comment c ON c.post = p
  WHERE p.id = :postId AND (p.scheduledAt IS NULL OR p.scheduledAt <= CURRENT_TIMESTAMP)
  GROUP BY p.id, a.id, p.durationSeconds
""")
    Optional<PostViewDto> findPostViewByPostId(
            @Param("postId") Long postId,
            @Param("currentUsername") String currentUsername
    );

    @Query("""
  SELECT new com.jutjubic.dto.PostViewDto(
      p.id,
      p.title,
      p.description,
      p.tags,
      p.videoUrl,
      CONCAT(CONCAT('/api/posts/', p.id), '/thumbnail'),
      p.createdAt,

      a.id,
      a.username,
      a.name,
      a.surname,
      a.profileImageUrl,

      COUNT(DISTINCT c),
      COUNT(DISTINCT l),
      false,
      p.viewCount,
      p.scheduledAt,
      p.durationSeconds
  )
  FROM Post p
  JOIN p.author a
  LEFT JOIN Comment c ON c.post = p
  LEFT JOIN PostLike l ON l.post = p
  WHERE p.id IN :ids
  AND (p.scheduledAt IS NULL OR p.scheduledAt <= CURRENT_TIMESTAMP)
  GROUP BY
      p.id, p.title, p.description, p.tags, p.videoUrl, p.createdAt,
      a.id, a.username, a.name, a.surname, a.profileImageUrl,
      p.viewCount, p.scheduledAt, p.durationSeconds
  ORDER BY array_position(:ids, p.id)
""")
    List<PostViewDto> findPopularPostsByIds(@Param("ids") List<Long> ids);


    @Modifying
    @Transactional
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :postId")
    void incrementViewCount(@Param("postId") Long postId);

}


