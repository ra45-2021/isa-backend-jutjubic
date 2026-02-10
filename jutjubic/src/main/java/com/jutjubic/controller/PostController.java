package com.jutjubic.controller;

import com.jutjubic.config.UploadProperties;
import com.jutjubic.domain.Post;
import com.jutjubic.dto.*;
import com.jutjubic.repository.PostRepository;
import com.jutjubic.service.CommentService;
import com.jutjubic.service.PostService;
import com.jutjubic.service.PostUploadService;
import com.jutjubic.service.ThumbnailService;
import com.jutjubic.service.VideoViewCrdtService;
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
import org.springframework.web.server.ResponseStatusException;
import com.jutjubic.domain.*;
import com.jutjubic.repository.*;
import java.util.Map;
import java.security.Principal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.support.ResourceRegion;



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
    private final UserRepository userRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostService postService;
    private final VideoViewCrdtService videoViewCrdtService;

    public PostController(
            PostRepository postRepository,
            CommentService commentService,
            ThumbnailService thumbnailService,
            PostUploadService postUploadService,
            UploadProperties uploadProperties,
            UserRepository userRepository,
            PostLikeRepository postLikeRepository,
            PostService postService,
            VideoViewCrdtService videoViewCrdtService) {
        this.postRepository = postRepository;
        this.commentService = commentService;
        this.thumbnailService = thumbnailService;
        this.postUploadService = postUploadService;
        this.uploadProperties = uploadProperties;
        this.userRepository = userRepository;
        this.postLikeRepository = postLikeRepository;
        this.postService = postService;
        this.videoViewCrdtService = videoViewCrdtService;
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
    public ResponseEntity<ResourceRegion> getVideo(
            @PathVariable Long postId,
            @RequestHeader HttpHeaders headers
    ) throws Exception {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        String videoUrl = post.getVideoUrl();
        if (videoUrl == null || videoUrl.isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Video URL is empty");
        }

        //postRepository.incrementViewCount(postId);

        String fileName = Paths.get(videoUrl).getFileName().toString();

        Path baseDir = Paths.get(uploadProperties.getDir()).normalize();
        Path filePath = baseDir
                .resolve(uploadProperties.getVideosTranscodedDir())
                .resolve(fileName)
                .normalize();

        if (!filePath.startsWith(baseDir)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid video path");
        }
        if (!Files.exists(filePath)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Transcoded video not found: " + filePath);
        }

        Resource video = new UrlResource(filePath.toUri());
        long contentLength = video.contentLength();

        final long chunkSize = 1024 * 1024;

        List<HttpRange> ranges = headers.getRange();
        ResourceRegion region;

        if (ranges == null || ranges.isEmpty()) {
            long rangeLength = Math.min(chunkSize, contentLength);
            region = new ResourceRegion(video, 0, rangeLength);
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .contentType(MediaTypeFactory.getMediaType(video).orElse(MediaType.APPLICATION_OCTET_STREAM))
                    .body(region);
        } else {
            HttpRange range = ranges.get(0);
            long start = range.getRangeStart(contentLength);
            long end = range.getRangeEnd(contentLength);
            long rangeLength = Math.min(chunkSize, end - start + 1);
            region = new ResourceRegion(video, start, rangeLength);

            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .contentType(MediaTypeFactory.getMediaType(video).orElse(MediaType.APPLICATION_OCTET_STREAM))
                    .body(region);
        }
    }


    @PostMapping("/{postId}/view")
    public ResponseEntity<Void> incrementView(@PathVariable Long postId) {
        videoViewCrdtService.incrementViewForReplica(postId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{postId}/crdt-views")
    public ResponseEntity<?> getCrdtViews(@PathVariable Long postId) {
        //videoViewCrdtService.broadcastToOtherReplicas(postId);

        Long totalViews = videoViewCrdtService.getTotalViewCount(postId);
        var counters = videoViewCrdtService.getAllCountersForVideo(postId);
        String currentReplicaId = videoViewCrdtService.getReplicaId();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

        var formattedCounters = counters.stream().map(c -> Map.of(
                "replicaId", c.getId().getReplicaId(),
                "viewCount", c.getViewCount(),
                "lastUpdated", Instant.ofEpochMilli(c.getLastUpdated())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime()
                        .format(formatter)
        )).collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
                "videoId", postId,
                "totalViews", totalViews,
                "currentReplica", currentReplicaId,
                "countersPerReplica", formattedCounters
        ));
    }

    @GetMapping("/{postId}/view-statistics")
    public ResponseEntity<?> viewStatistics(@PathVariable Long postId) {

        videoViewCrdtService.hardSyncAllReplicas(postId);

        Long totalViews = videoViewCrdtService.getTotalViewCount(postId);
        var counters = videoViewCrdtService.getAllCountersForVideo(postId);

        return ResponseEntity.ok(Map.of(
                "videoId", postId,
                "totalViews", totalViews,
                "countersPerReplica", counters.stream().map(c -> Map.of(
                        "replicaId", c.getId().getReplicaId(),
                        "viewCount", c.getViewCount()
                )).toList()
        ));
    }

    @GetMapping
    public List<PostViewDto> getAllPosts(Authentication auth) {
        String currentUsername = (auth != null) ? auth.getName() : null;
        List<PostViewDto> list = postRepository.findAllPostViewsNewestFirst(currentUsername);

        for (PostViewDto dto : list) {
            Long total = videoViewCrdtService.getTotalViewCount(dto.getId());
            dto.setViewCount(total != null ? total : 0L);
        }
        return list;
    }


    @GetMapping("/{postId}/comments")
    public CommentPageDto getComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size
    ) {
        return commentService.getComments(postId, page, size);
    }

    @PostMapping("/{postId}/comments")
    public CommentViewDto addComment(
            @PathVariable Long postId,
            @RequestBody CreateCommentRequestDto req,
            Authentication authentication
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        return commentService.addComment(postId, req.getText());
    }

    @GetMapping("/by-user/{username}")
    public List<PostViewDto> postsByUser(@PathVariable String username, Authentication auth) {
        String currentUsername = (auth != null) ? auth.getName() : null;
        return postRepository.findAllPostViewsByUsernameNewestFirst(username, currentUsername);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostViewDto> getPost(@PathVariable Long postId, Authentication auth) {
        String currentUsername = (auth != null) ? auth.getName() : null;

        return postRepository.findPostViewByPostId(postId, currentUsername)
                .map(dto -> {
                    dto.setViewCount(videoViewCrdtService.getTotalViewCount(postId));
                    return ResponseEntity.ok(dto);
                })
                .orElse(ResponseEntity.notFound().build());
    }


    @PostMapping("/{postId}/like")
    public ResponseEntity<?> toggleLike(@PathVariable Long postId, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userRepository.findByEmailAdress(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        PostLikeId likeId = new PostLikeId(post.getId(), user.getId());

        boolean alreadyLiked = postLikeRepository.existsById(likeId);
        if (alreadyLiked) {
            postLikeRepository.deleteById(likeId);
        } else {
            postLikeRepository.save(new PostLike(post, user));
        }

        long count = postLikeRepository.countByPostId(postId);
        return ResponseEntity.ok(Map.of(
                "likes", count,
                "isLiked", !alreadyLiked
        ));
    }


}