package com.jutjubic.service;

import com.jutjubic.config.UploadProperties;
import com.jutjubic.domain.Post;
import com.jutjubic.repository.PostRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ThumbnailService {

    private final PostRepository postRepository;
    private final UploadProperties uploadProperties;

    public ThumbnailService(PostRepository postRepository, UploadProperties uploadProperties) {
        this.postRepository = postRepository;
        this.uploadProperties = uploadProperties;
    }

    @Cacheable(value = "thumbnails", key = "#postId")
    public byte[] getThumbnailBytes(Long postId) {
        System.out.println("🔥 CACHE MISS - Loading thumbnail from disk for postId: " + postId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        String thumbnailUrl = post.getThumbnailUrl();
        if (thumbnailUrl == null || thumbnailUrl.isBlank()) {
            throw new RuntimeException("Thumbnail URL is empty");
        }

        String thumbFileName = Paths.get(thumbnailUrl).getFileName().toString();

        Path baseDir = Paths.get(uploadProperties.getDir());
        if (!baseDir.isAbsolute()) {
            baseDir = Paths.get(System.getProperty("user.dir")).resolve(baseDir);
        }

        Path filePath = baseDir
                .resolve(uploadProperties.getThumbsDir())
                .resolve(thumbFileName)
                .toAbsolutePath()
                .normalize();

        if (!Files.exists(filePath)) {
            throw new RuntimeException("Thumbnail file not found: " + filePath);
        }

        try {
            byte[] bytes = Files.readAllBytes(filePath);
            System.out.println("✅ Thumbnail loaded successfully: " + bytes.length + " bytes");
            return bytes;
        } catch (IOException e) {
            throw new RuntimeException("Error reading thumbnail", e);
        }
    }
}