package com.jutjubic.controller;

import com.jutjubic.domain.Post;
import com.jutjubic.dto.PostViewDto;
import com.jutjubic.dto.UserDto;
import com.jutjubic.repository.PostRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@CrossOrigin
public class PostController {

    private final PostRepository postRepository;

    public PostController(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @GetMapping
    public List<PostViewDto> getAllPosts() {
        return postRepository.findAllWithAuthorNewestFirst()
                .stream()
                .map(this::toViewDto)
                .toList();
    }

    private PostViewDto toViewDto(Post p) {
        var a = p.getAuthor();
        return new PostViewDto(
                p.getId(),
                p.getTitle(),
                p.getDescription(),
                p.getVideoUrl(),
                p.getThumbnailUrl(),
                p.getCreatedAt(),
                new UserDto(a.getId(), a.getUsername(), a.getDisplayName())
        );
    }
}
