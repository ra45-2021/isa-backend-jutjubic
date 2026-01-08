package com.jutjubic.service;

import com.jutjubic.domain.Post;
import com.jutjubic.repository.PostRepository;
import com.jutjubic.repository.UserRepository;
import com.jutjubic.domain.User;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

@Service
public class PostUploadService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final LocalUploadStorageService storage;

    public PostUploadService(
            PostRepository postRepository,
            UserRepository userRepository,
            LocalUploadStorageService storage
    ) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.storage = storage;
    }

    /**
     * Transakciono kreiranje posta:
     * - upload u temp
     * - DB insert
     * - temp -> final
     * - rollback DB + fajlova ako bilo šta pukne
     */
    @Transactional
    public Post createPost(
            String authorEmail,
            String title,
            String description,
            String tags,
            Double locationLat,
            Double locationLon,
            MultipartFile thumbnail,
            MultipartFile video
    ) throws IOException {

        // 0) validacije (minimalne)
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (video == null || video.isEmpty()) {
            throw new IllegalArgumentException("Video file is required");
        }
        if (!video.getOriginalFilename().toLowerCase().endsWith(".mp4")) {
            throw new IllegalArgumentException("Video must be mp4");
        }

        // 1) upload u TEMP (ovde se već meri timeout u storage servisu)
        LocalUploadStorageService.TempFiles temp;
        try {
            temp = storage.saveToTemp(video, thumbnail);
        } catch (Exception e) {
            throw new RuntimeException("Upload failed: " + e.getMessage(), e);
        }

        Path finalVideo = null;
        Path finalThumb = null;

        try {
            // 2) DB insert (u istoj transakciji)
            Post saved = createDbRecord(
                    authorEmail,
                    title,
                    description,
                    tags,
                    locationLat,
                    locationLon,
                    temp.videoName(),
                    temp.thumbName()
            );

            // 3) TEMP -> FINAL
            var finals = storage.moveToFinal(temp);
            finalVideo = finals.videoPath();
            finalThumb = finals.thumbPath();

            // 4) setuj stabilne API URL-ove (frontend će koristiti ove endpoint-e)
            String videoFileName = finals.videoPath().getFileName().toString();
            String thumbFileName = finals.thumbPath().getFileName().toString();

            saved.setVideoUrl("/media/videos/" + videoFileName);
            saved.setThumbnailUrl("/media/thumbs/" + thumbFileName);

            // 5) cleanup temp (ako moveToFinal radi copy umesto move)
            storage.deleteIfExists(temp.tempVideo());
            storage.deleteIfExists(temp.tempThumb());

            return postRepository.save(saved);

        } catch (Exception e) {
            // rollback fajlova
            storage.deleteIfExists(finalVideo);
            storage.deleteIfExists(finalThumb);
            storage.deleteIfExists(temp.tempVideo());
            storage.deleteIfExists(temp.tempThumb());
            throw e;
        }
    }

    /**
     * Kreira Post u bazi.
     * NEMA @Transactional – oslanja se na spoljašnju transakciju.
     */
    private Post createDbRecord(
            String authorEmail,
            String title,
            String description,
            String tags,
            Double locationLat,
            Double locationLon,
            String tempVideoName,
            String tempThumbName
    ) {

        User author = userRepository.findByEmailAdress(authorEmail)
                .orElseThrow(() -> new RuntimeException("Author not found"));

        Post post = new Post();
        post.setAuthor(author);
        post.setTitle(title);
        post.setDescription(description);
        post.setTags(tags);
        post.setLocationLat(locationLat);
        post.setLocationLon(locationLon);
        post.setCreatedAt(Instant.now());

        // privremeni URL-ovi (biće zamenjeni posle moveToFinal)
        post.setVideoUrl(tempVideoName);
        post.setThumbnailUrl(tempThumbName);

        return postRepository.save(post);
    }
}
