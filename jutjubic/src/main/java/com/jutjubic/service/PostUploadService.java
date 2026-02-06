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
import com.jutjubic.dto.TranscodeJobMessageDto;
import java.util.UUID;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

@Service
public class PostUploadService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final LocalUploadStorageService storage;
    private final TranscodePublisher transcodePublisher;
    private final UploadEventProducer uploadEventProducer;

    public PostUploadService(
            PostRepository postRepository,
            UserRepository userRepository,
            LocalUploadStorageService storage,
            TranscodePublisher transcodePublisher,
            UploadEventProducer uploadEventProducer
    ) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.storage = storage;
        this.transcodePublisher = transcodePublisher;
        this.uploadEventProducer = uploadEventProducer;
    }

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

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (video == null || video.isEmpty()) {
            throw new IllegalArgumentException("Video file is required");
        }
        if (!video.getOriginalFilename().toLowerCase().endsWith(".mp4")) {
            throw new IllegalArgumentException("Video must be mp4");
        }

        LocalUploadStorageService.TempFiles temp;
        try {
            temp = storage.saveToTemp(video, thumbnail);
        } catch (Exception e) {
            throw new RuntimeException("Upload failed: " + e.getMessage(), e);
        }

        try {
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

            String videoFileName = temp.videoName();
            String thumbFileName = temp.thumbName();

            saved.setVideoUrl("/media/videos/" + videoFileName);
            saved.setThumbnailUrl("/media/thumbs/" + thumbFileName);

            Post finalSaved = postRepository.save(saved);

            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    System.out.println("Transaction committed successfully - moving files to final location");
                    try {
                        var finals = storage.moveToFinal(temp);
                        System.out.println("Files moved successfully: " + finals.videoPath());

                        String jobId = UUID.randomUUID().toString();
                        String inputAbsPath = finals.videoPath().toAbsolutePath().toString();

                        transcodePublisher.publish(new TranscodeJobMessageDto(
                                jobId,
                                finalSaved.getId(),
                                inputAbsPath,
                                "mp4_720p"
                        ));

                        System.out.println("PUBLISHED TRANSCODE JOB: postId=" + finalSaved.getId() + " input=" + inputAbsPath);

                        // Šalji UploadEvent u RabbitMQ (JSON i Protobuf format) za benchmark
                        long fileSizeBytes = finals.videoPath().toFile().length();
                        uploadEventProducer.sendUploadEvent(finalSaved, fileSizeBytes);
                        System.out.println("PUBLISHED UPLOAD EVENT: postId=" + finalSaved.getId() + " fileSize=" + fileSizeBytes);

                        storage.deleteIfExists(temp.tempVideo());
                        storage.deleteIfExists(temp.tempThumb());

                    } catch (IOException e) {
                        System.err.println("ERROR: Failed to move files after commit: " + e.getMessage());
                    } catch (Exception e) {
                        System.err.println("ERROR: Failed to publish transcode job: " + e.getMessage());
                    }
                }

                @Override
                public void afterCompletion(int status) {
                    if (status == STATUS_ROLLED_BACK) {
                        System.out.println(" Transaction rolled back - cleaning up temp files");
                        storage.deleteIfExists(temp.tempVideo());
                        storage.deleteIfExists(temp.tempThumb());
                    }
                }
            });

            return finalSaved;

        } catch (Exception e) {
            storage.deleteIfExists(temp.tempVideo());
            storage.deleteIfExists(temp.tempThumb());
            throw e;
        }
    }

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

        post.setVideoUrl(tempVideoName);
        post.setThumbnailUrl(tempThumbName);

        return postRepository.save(post);
    }
}
