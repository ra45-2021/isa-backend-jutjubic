package com.jutjubic.controller;

import com.jutjubic.config.UploadProperties;
import com.jutjubic.domain.Post;
import com.jutjubic.dto.*;
import com.jutjubic.repository.PostRepository;
import com.jutjubic.service.CommentService;
import com.jutjubic.service.PostUploadService;
import com.jutjubic.service.ThumbnailService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.Part;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
@CrossOrigin
public class PostController {

    private final PostRepository postRepository;
    private final CommentService commentService;
    private final ThumbnailService thumbnailService;
    private final PostUploadService postUploadService;
    private final UploadProperties uploadProperties;

    public PostController(
            PostRepository postRepository,
            CommentService commentService,
            ThumbnailService thumbnailService,
            PostUploadService postUploadService, UploadProperties uploadProperties) {
        this.postRepository = postRepository;
        this.commentService = commentService;
        this.thumbnailService = thumbnailService;
        this.postUploadService = postUploadService;
        this.uploadProperties = uploadProperties;
    }

    @GetMapping("/{postId}/thumbnail")
    public ResponseEntity<byte[]> getThumbnail(@PathVariable Long postId) throws Exception {
        byte[] bytes = thumbnailService.getThumbnailBytes(postId);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(bytes);
    }

    @PostMapping
    public ResponseEntity<?> createPost(HttpServletRequest request) throws Exception {

        // Pronađi MultipartHttpServletRequest u lancu wrappera
        MultipartHttpServletRequest multipartRequest = null;
        HttpServletRequest currentRequest = request;

        while (currentRequest != null) {
            if (currentRequest instanceof MultipartHttpServletRequest) {
                multipartRequest = (MultipartHttpServletRequest) currentRequest;
                break;
            }
            if (currentRequest instanceof HttpServletRequestWrapper) {
                currentRequest = (HttpServletRequest) ((HttpServletRequestWrapper) currentRequest).getRequest();
            } else {
                break;
            }
        }

        if (multipartRequest == null) {
            return ResponseEntity.badRequest().body("Cannot process multipart request");
        }

        // Extract files using Parts API
        MultipartFile thumbnail = null;
        MultipartFile video = null;

        try {
            Part thumbnailPart = request.getPart("thumbnail");
            Part videoPart = request.getPart("video");

            if (thumbnailPart != null) {
                thumbnail = convertPartToMultipartFile(thumbnailPart);
            }
            if (videoPart != null) {
                video = convertPartToMultipartFile(videoPart);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error processing file uploads: " + e.getMessage());
        }

        // Extract parameters
        String title = request.getParameter("title");
        String description = request.getParameter("description");
        String tags = request.getParameter("tags");

        Double locationLat = null;
        Double locationLon = null;

        String latParam = request.getParameter("locationLat");
        String lonParam = request.getParameter("locationLon");

        if (latParam != null && !latParam.isBlank()) {
            locationLat = Double.parseDouble(latParam);
        }
        if (lonParam != null && !lonParam.isBlank()) {
            locationLon = Double.parseDouble(lonParam);
        }

        // Get authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        var created = postUploadService.createPost(
                email, title, description, tags, locationLat, locationLon, thumbnail, video
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    private MultipartFile convertPartToMultipartFile(Part part) {
        return new MultipartFile() {
            @Override
            public String getName() {
                return part.getName();
            }

            @Override
            public String getOriginalFilename() {
                return part.getSubmittedFileName();
            }

            @Override
            public String getContentType() {
                return part.getContentType();
            }

            @Override
            public boolean isEmpty() {
                return part.getSize() == 0;
            }

            @Override
            public long getSize() {
                return part.getSize();
            }

            @Override
            public byte[] getBytes() throws IOException {
                return part.getInputStream().readAllBytes();
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return part.getInputStream();
            }

            @Override
            public void transferTo(File dest) throws IOException {
                part.write(dest.getAbsolutePath());
            }
        };
    }

    @GetMapping("/{postId}/video")
    public ResponseEntity<byte[]> getVideo(@PathVariable Long postId) throws Exception {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        String videoUrl = post.getVideoUrl();
        if (videoUrl == null || videoUrl.isBlank()) {
            throw new RuntimeException("Video URL is empty");
        }

        // Extract filename
        String videoFileName = Paths.get(videoUrl).getFileName().toString();

        Path baseDir = Paths.get(uploadProperties.getDir());
        if (!baseDir.isAbsolute()) {
            baseDir = Paths.get(System.getProperty("user.dir")).resolve(baseDir);
        }

        Path filePath = baseDir
                .resolve(uploadProperties.getVideosDir())
                .resolve(videoFileName)
                .toAbsolutePath()
                .normalize();

        if (!Files.exists(filePath)) {
            throw new RuntimeException("Video file not found: " + filePath);
        }

        byte[] videoBytes = Files.readAllBytes(filePath);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("video/mp4"))
                .body(videoBytes);
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