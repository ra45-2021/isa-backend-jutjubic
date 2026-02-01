package com.jutjubic.service;

import com.jutjubic.config.UploadProperties;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class ThumbnailCompressionService {

    private final UploadProperties props;

    public ThumbnailCompressionService(UploadProperties props) {
        this.props = props;
    }

    @Scheduled(cron = "*/15 * * * * *", zone = "Europe/Belgrade")
    public void dailyCompressionJob() {
        System.out.println("[THUMB-COMPRESS] Daily job started...");
        var res = compressThumbnailsOlderThanDays(30);
        System.out.println("[THUMB-COMPRESS] Done. scanned=" + res.scanned()
                + " eligible=" + res.eligible()
                + " compressed=" + res.compressed()
                + " skipped=" + res.skipped()
                + " failed=" + res.failed());
    }

    public record CompressionResult(int scanned, int eligible, int compressed, int skipped, int failed) {}

    public CompressionResult compressThumbnailsOlderThanDays(int days) {
        Path baseDir = Paths.get(props.getDir());
        if (!baseDir.isAbsolute()) {
            baseDir = Paths.get(System.getProperty("user.dir")).resolve(baseDir);
        }
        baseDir = baseDir.toAbsolutePath().normalize();

        Path thumbsDir = baseDir.resolve(props.getThumbsDir()).normalize();
        Path compressedDir = baseDir.resolve(props.getThumbsCompressedDir()).normalize();

        int scanned = 0, eligible = 0, compressed = 0, skipped = 0, failed = 0;

        try {
            Files.createDirectories(thumbsDir);
            Files.createDirectories(compressedDir);

            Instant cutoff = Instant.now().minus(days, ChronoUnit.DAYS);

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(thumbsDir)) {
                for (Path original : stream) {
                    if (Files.isDirectory(original)) continue;
                    scanned++;

                    String name = original.getFileName().toString().toLowerCase();
                    if (!(name.endsWith(".jpg") || name.endsWith(".jpeg"))) {
                        // Thumbnailator outputQuality radi najbolje za JPEG.
                        skipped++;
                        continue;
                    }

                    Instant lastModified = Files.getLastModifiedTime(original).toInstant();
                    if (lastModified.isAfter(cutoff)) {
                        skipped++;
                        continue;
                    }

                    eligible++;

                    Path compressedFile = compressedDir.resolve(original.getFileName().toString());
                    if (Files.exists(compressedFile)) {
                        skipped++;
                        continue; // već kompresovano
                    }

                    try {
                        // Kompresija: zadrži dimenzije (scale(1.0)), spusti kvalitet
                        Thumbnails.of(original.toFile())
                                .scale(1.0)
                                .outputQuality(0.75) // 0.0 - 1.0
                                .outputFormat("jpg")
                                .toFile(compressedFile.toFile());

                        compressed++;
                        System.out.println("[THUMB-COMPRESS] Compressed: " + original + " -> " + compressedFile);

                    } catch (Exception e) {
                        failed++;
                        System.err.println("[THUMB-COMPRESS] FAILED for " + original + " : " + e.getMessage());
                    }
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Thumbnail compression scan failed: " + e.getMessage(), e);
        }

        return new CompressionResult(scanned, eligible, compressed, skipped, failed);
    }
}
