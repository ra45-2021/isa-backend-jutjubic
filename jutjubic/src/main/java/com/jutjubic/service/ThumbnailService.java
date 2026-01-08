package com.jutjubic.service;

import com.jutjubic.config.UploadProperties;
import com.jutjubic.domain.Post;
import com.jutjubic.repository.PostRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ThumbnailService {

    private final PostRepository postRepository;
    private final UploadProperties props;
    private final UploadProperties uploadProperties;

    public ThumbnailService(PostRepository postRepository, UploadProperties props, UploadProperties uploadProperties) {
        this.postRepository = postRepository;
        this.props = props;
        this.uploadProperties = uploadProperties;
    }

    @Cacheable(value = "thumbnails", key = "#postId")
    public byte[] getThumbnailBytes(Long postId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        String thumbnailUrl = post.getThumbnailUrl();
        if (thumbnailUrl == null || thumbnailUrl.isBlank()) {
            throw new RuntimeException("Thumbnail URL is empty");
        }

        // ✅ OVO je thumbFileName
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
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Error reading thumbnail", e);
        }
    }



    private String extractFileNameFromInternalUrl(String url) {
        // radi i za /_files/thumbs/x.png i za bilo koji url sa poslednjim segmentom kao naziv fajla
        int idx = url.lastIndexOf('/');
        if (idx < 0 || idx == url.length() - 1) throw new IllegalArgumentException("Invalid thumbnail url: " + url);
        return url.substring(idx + 1);
    }
}
