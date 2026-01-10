package com.jutjubic.service;

import com.jutjubic.domain.Post;
import com.jutjubic.repository.PostRepository;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class PostService {

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Async
    public void incrementViewCount(Long postId) {
        postRepository.incrementViewCount(postId);
    }
}