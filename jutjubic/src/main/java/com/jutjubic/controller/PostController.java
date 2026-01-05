package com.jutjubic.controller;

import com.jutjubic.domain.Post;
import com.jutjubic.dto.*;
import com.jutjubic.repository.PostRepository;
import com.jutjubic.service.CommentService;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
@CrossOrigin
public class PostController {

    private final PostRepository postRepository;
    private final CommentService commentService;

    public PostController(
            PostRepository postRepository,
            CommentService commentService
    ) {
        this.postRepository = postRepository;
        this.commentService = commentService;
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

        List<String> tags = List.of();
        if (p.getTags() != null && !p.getTags().isBlank()) {
            tags = Arrays.stream(p.getTags().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .toList();
        }

        return new PostViewDto(
                p.getId(),
                p.getTitle(),
                p.getDescription(),
                tags,
                p.getVideoUrl(),
                p.getThumbnailUrl(),
                p.getCreatedAt(),
                new UserDto(
                        a.getId(),
                        a.getUsername(),
                        a.getName(),
                        a.getSurname(),
                        a.getProfileImageUrl()
                )
        );
    }


    @GetMapping("/{postId}/comments")
    public CommentPageDto getComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size
    ) {
        return commentService.getComments(postId, page, size);
    }

    @PostMapping("/{postId}/comments")
    public CommentViewDto addComment(
            @PathVariable Long postId,
            @RequestParam String authorUsername,
            @RequestBody CreateCommentRequestDto req
    ) {
        return commentService.addComment(postId, authorUsername, req.getText());
    }
}
