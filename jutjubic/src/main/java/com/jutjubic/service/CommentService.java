package com.jutjubic.service;

import com.jutjubic.domain.Comment;
import com.jutjubic.domain.Post;
import com.jutjubic.dto.CommentPageDto;
import com.jutjubic.dto.CommentViewDto;
import com.jutjubic.repository.CommentRepository;
import com.jutjubic.repository.PostRepository;
import com.jutjubic.repository.UserRepository;
import com.jutjubic.security.CommentRateLimiter;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;


import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRateLimiter commentRateLimiter;


    public CommentService(
            CommentRepository commentRepository,
            PostRepository postRepository,
            UserRepository userRepository,
            CommentRateLimiter commentRateLimiter
    ) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.commentRateLimiter = commentRateLimiter;
    }


    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = "postComments",
            key = "#postId + ':' + #page + ':' + #size"
    )
    public CommentPageDto getComments(Long postId, int page, int size) {
        if (size < 1) size = 1;
        if (size > 10) size = 10;
        if (page < 0) page = 0;

        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var result = commentRepository.findByPost_IdOrderByCreatedAtDesc(postId, pageable);

        List<CommentViewDto> items = result.getContent().stream()
                .map(c -> new CommentViewDto(
                        c.getId(),
                        c.getAuthor().getUsername(),
                        c.getAuthor().getProfileImageUrl(),
                        c.getCreatedAt(),
                        c.getText()
                ))
                .toList();

        return new CommentPageDto(
                items,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    @Transactional
    @CacheEvict(cacheNames = "postComments", allEntries = true)
    public CommentViewDto addComment(Long postId, String text) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        String authorEmail = authentication.getName();


        String t = text == null ? "" : text.trim();
        if (t.isEmpty()) {
            throw new IllegalArgumentException("Comment text is required");
        }

        Post post = postRepository.findById(postId).orElseThrow();

        var author = userRepository
                .findByEmailAdress(authorEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "User not found"
                ));


        if (!author.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "User is not active"
            );
        }
        commentRateLimiter.assertAllowed(authorEmail);

        Comment saved = commentRepository.save(new Comment(post, author, t));

        return new CommentViewDto(
                saved.getId(),
                author.getUsername(),
                author.getProfileImageUrl(),
                saved.getCreatedAt(),
                saved.getText()
        );
    }



}
