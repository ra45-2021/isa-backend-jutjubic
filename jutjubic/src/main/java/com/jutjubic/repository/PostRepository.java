package com.jutjubic.repository;

import com.jutjubic.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.jutjubic.dto.PostViewDto;

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

      COUNT(c.id)
  )
  FROM Post p
  JOIN p.author a
  LEFT JOIN Comment c ON c.post = p
  GROUP BY
      p.id, p.title, p.description, p.tags, p.videoUrl, p.createdAt,
      a.id, a.username, a.name, a.surname, a.profileImageUrl
  ORDER BY p.createdAt DESC
""")
    List<PostViewDto> findAllPostViewsNewestFirst();

}


