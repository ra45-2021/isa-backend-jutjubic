package com.jutjubic.service;

import com.jutjubic.domain.Post;
import com.jutjubic.repository.PostRepository;
import com.jutjubic.repository.UserRepository;
import com.jutjubic.domain.User;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
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
     * - NAKON uspešnog commit-a: temp -> final
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

            // 3) Pripremi finalne URL-ove (ali još uvek ne premesti fajlove!)
            String videoFileName = temp.videoName();
            String thumbFileName = temp.thumbName();

            saved.setVideoUrl("/media/videos/" + videoFileName);
            saved.setThumbnailUrl("/media/thumbs/" + thumbFileName);

            Post finalSaved = postRepository.save(saved);

            // 4) Registruj callback koji se izvršava NAKON uspešnog commit-a transakcije
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    // OVDE se fajlovi pomeraju SAMO ako je transakcija uspela!
                    System.out.println(" Transaction committed successfully - moving files to final location");
                    try {
                        var finals = storage.moveToFinal(temp);
                        System.out.println(" Files moved successfully: " + finals.videoPath());

                        // Cleanup temp fajlova
                        storage.deleteIfExists(temp.tempVideo());
                        storage.deleteIfExists(temp.tempThumb());
                    } catch (IOException e) {
                        System.err.println(" ERROR: Failed to move files after commit: " + e.getMessage());
                        // Ovde je transakcija već committed, ali fajlovi nisu premešteni
                        // Idealno bi bilo logovati ovu grešku u monitoring sistem
                        // U produkciji bi trebao cleanup job koji briše orphaned temp fajlove
                    }
                }

                @Override
                public void afterCompletion(int status) {
                    if (status == STATUS_ROLLED_BACK) {
                        // Transakcija je rollback-ovana - obriši temp fajlove
                        System.out.println(" Transaction rolled back - cleaning up temp files");
                        storage.deleteIfExists(temp.tempVideo());
                        storage.deleteIfExists(temp.tempThumb());
                    }
                }
            });

            return finalSaved;

        } catch (Exception e) {
            // rollback temp fajlova (finalni još ne postoje jer se nisu pomerili)
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
