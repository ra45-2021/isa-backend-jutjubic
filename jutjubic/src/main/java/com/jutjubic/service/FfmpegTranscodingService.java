package com.jutjubic.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

@Service
public class FfmpegTranscodingService {

    public void transcode720p(Path input, Path output) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-y",
                "-i", input.toString(),

                // resize
                "-vf", "scale=-2:720",

                // video: web-safe H.264
                "-c:v", "libx264",
                "-pix_fmt", "yuv420p",
                "-profile:v", "main",
                "-level", "4.0",
                "-preset", "veryfast",
                "-crf", "23",

                // audio: AAC
                "-c:a", "aac",
                "-b:a", "128k",

                "-movflags", "+faststart",

                output.toString()
        );

        pb.redirectErrorStream(true);
        Process p = pb.start();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
            while (br.readLine() != null) { /* consume */ }
        }

        int exit = p.waitFor();
        if (exit != 0) throw new RuntimeException("ffmpeg failed with exit code " + exit);
    }
}
