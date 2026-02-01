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

    @Cacheable(value = "thumbnails", key = "#postId + ':' + (#root.target.isCompressedAvailable(#postId) ? 'c' : 'o')")
    public byte[] getThumbnailBytes(Long postId) {
        boolean useCompressed = isCompressedAvailable(postId);
        System.out.println("🔥 CACHE MISS - Loading thumbnail from disk for postId: " + postId
                + " (useCompressed=" + useCompressed + ")");

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
        baseDir = baseDir.toAbsolutePath().normalize();

        Path originalPath = baseDir
                .resolve(uploadProperties.getThumbsDir())
                .resolve(thumbFileName)
                .toAbsolutePath()
                .normalize();

        Path compressedPath = baseDir
                .resolve(uploadProperties.getThumbsCompressedDir())
                .resolve(thumbFileName)
                .toAbsolutePath()
                .normalize();

        Path filePath = Files.exists(compressedPath) ? compressedPath : originalPath;

        if (!Files.exists(filePath)) {
            throw new RuntimeException("Thumbnail file not found: " + filePath);
        }

        try {
            byte[] bytes = Files.readAllBytes(filePath);
            System.out.println("✅ Thumbnail loaded successfully: " + bytes.length + " bytes"
                    + " (" + (filePath.equals(compressedPath) ? "COMPRESSED" : "ORIGINAL") + ")");
            return bytes;
        } catch (IOException e) {
            throw new RuntimeException("Error reading thumbnail", e);
        }
    }

    public boolean isCompressedAvailable(Long postId) {
        Post post = postRepository.findById(postId).orElse(null);
        if (post == null) return false;

        String thumbnailUrl = post.getThumbnailUrl();
        if (thumbnailUrl == null || thumbnailUrl.isBlank()) return false;

        String thumbFileName = Paths.get(thumbnailUrl).getFileName().toString();

        Path baseDir = Paths.get(uploadProperties.getDir());
        if (!baseDir.isAbsolute()) {
            baseDir = Paths.get(System.getProperty("user.dir")).resolve(baseDir);
        }
        baseDir = baseDir.toAbsolutePath().normalize();

        Path compressedPath = baseDir
                .resolve(uploadProperties.getThumbsCompressedDir())
                .resolve(thumbFileName)
                .toAbsolutePath()
                .normalize();

        return Files.exists(compressedPath);
    }
}
