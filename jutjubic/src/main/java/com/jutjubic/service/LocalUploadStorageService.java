package com.jutjubic.service;

import com.jutjubic.config.UploadProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class LocalUploadStorageService {

    private final UploadProperties props;
    private boolean testSlowMode = false;

    public LocalUploadStorageService(UploadProperties props) {
        this.props = props;
    }

    public record TempFiles(Path tempVideo, Path tempThumb, String videoName, String thumbName) {}
    public record FinalFiles(Path videoPath, Path thumbPath) {}

    public TempFiles saveToTemp(MultipartFile video, MultipartFile thumbnail) throws IOException {
        validateVideo(video);
        validateThumbnail(thumbnail);

        Path base = Paths.get(props.getDir()).toAbsolutePath().normalize();
        Path tmp = base.resolve(props.getTmpDir());
        Files.createDirectories(tmp);

        String id = UUID.randomUUID().toString();
        String videoName = id + ".mp4";
        String thumbName = id + safeImageExt(thumbnail.getOriginalFilename());

        Path tempVideo = tmp.resolve(videoName + ".part");
        Path tempThumb = tmp.resolve(thumbName + ".part");

        try {
            copyWithTimeout(video.getInputStream(), tempVideo, props.getTimeoutSeconds());
            copyWithTimeout(thumbnail.getInputStream(), tempThumb, props.getTimeoutSeconds());
            return new TempFiles(tempVideo, tempThumb, videoName, thumbName);
        } catch (Exception e) {
            safeDelete(tempVideo);
            safeDelete(tempThumb);
            if (e instanceof IOException io) throw io;
            throw new IOException("Upload failed or exceeded timeout.", e);
        }
    }

    public FinalFiles moveToFinal(TempFiles temp) throws IOException {
        Path base = Paths.get(props.getDir()).toAbsolutePath().normalize();
        Path videos = base.resolve(props.getVideosDir());
        Path thumbs = base.resolve(props.getThumbsDir());

        Files.createDirectories(videos);
        Files.createDirectories(thumbs);

        Path finalVideo = videos.resolve(temp.videoName());
        Path finalThumb = thumbs.resolve(temp.thumbName());

        try {
            Files.move(temp.tempVideo(), finalVideo, StandardCopyOption.REPLACE_EXISTING);
            Files.move(temp.tempThumb(), finalThumb, StandardCopyOption.REPLACE_EXISTING);
            return new FinalFiles(finalVideo, finalThumb);
        } catch (Exception e) {
            safeDelete(finalVideo);
            safeDelete(finalThumb);
            throw e;
        }
    }

    public void deleteIfExists(Path p) {
        safeDelete(p);
    }

    private void validateVideo(MultipartFile video) {
        if (video == null || video.isEmpty()) throw new IllegalArgumentException("Video is required.");
        if (video.getSize() > 200L * 1024 * 1024) throw new IllegalArgumentException("Video max size is 200MB.");

        String ct = video.getContentType();
        if (ct == null || !ct.equalsIgnoreCase("video/mp4")) {
            throw new IllegalArgumentException("Video must be video/mp4.");
        }
    }

    private void validateThumbnail(MultipartFile thumb) {
        if (thumb == null || thumb.isEmpty()) throw new IllegalArgumentException("Thumbnail is required.");
        String ct = thumb.getContentType();
        if (ct == null || !ct.startsWith("image/")) {
            throw new IllegalArgumentException("Thumbnail must be an image.");
        }
    }

    private String safeImageExt(String originalName) {
        if (originalName == null) return ".png";
        String lower = originalName.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return ".jpg";
        if (lower.endsWith(".png")) return ".png";
        if (lower.endsWith(".webp")) return ".webp";
        return ".png";
    }

    private void copyWithTimeout(InputStream input, Path target, long timeoutSeconds) throws IOException {
        Instant start = Instant.now();

        if (testSlowMode) {
            try {
                System.out.println("TEST SLOW MODE: Waiting 21 seconds to simulate slow upload...");
                Thread.sleep(21000); // 21 sekundi
                System.out.println("TEST SLOW MODE: Sleep finished, now checking timeout...");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Upload interrupted", e);
            }
        }

        try (BufferedInputStream in = new BufferedInputStream(input);
             OutputStream out = Files.newOutputStream(target, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

            byte[] buf = new byte[1024 * 1024]; // 1MB buffer
            int r;
            while ((r = in.read(buf)) != -1) {
                out.write(buf, 0, r);

                if (Duration.between(start, Instant.now()).getSeconds() > timeoutSeconds) {
                    throw new IOException("Upload exceeded timeout of " + timeoutSeconds + " seconds.");
                }
            }
        }
    }

    private void safeDelete(Path p) {
        try {
            if (p != null) Files.deleteIfExists(p);
        } catch (Exception ignored) {}
    }
}
