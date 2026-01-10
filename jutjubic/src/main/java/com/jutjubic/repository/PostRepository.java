package com.jutjubic.repository;

import com.jutjubic.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.jutjubic.dto.PostViewDto;
import org.springframework.data.repository.query.Param;
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
      (SELECT COUNT(l) > 0 FROM PostLike l WHERE l.post = p AND l.user.username = :currentUsername)
  )
  FROM Post p
  JOIN p.author a
  LEFT JOIN Comment c ON c.post = p
  GROUP BY
      p.id, p.title, p.description, p.tags, p.videoUrl, p.createdAt,
      a.id, a.username, a.name, a.surname, a.profileImageUrl
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
      (SELECT COUNT(l) > 0 FROM PostLike l WHERE l.post = p AND l.user.username = :currentUsername)
  )
  FROM Post p
  JOIN p.author a
  LEFT JOIN Comment c ON c.post = p
  WHERE a.username = :username
  GROUP BY
      p.id, p.title, p.description, p.tags, p.videoUrl, p.createdAt,
      a.id, a.username, a.name, a.surname, a.profileImageUrl
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
      (SELECT COUNT(l) > 0 FROM PostLike l WHERE l.post = p AND l.user.username = :currentUsername)
  )
  FROM Post p
  JOIN p.author a
  LEFT JOIN Comment c ON c.post = p
  WHERE p.id = :postId
  GROUP BY p.id, a.id
""")
    Optional<PostViewDto> findPostViewByPostId(
            @Param("postId") Long postId,
            @Param("currentUsername") String currentUsername
    );

}


