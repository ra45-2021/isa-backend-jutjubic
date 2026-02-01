package com.jutjubic.service;

import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
public class FfmpegTranscodingService {

    public void transcode720p(Path input, Path output) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-y",
                "-i", input.toString(),
                "-vf", "scale=-2:720",
                "-c:v", "libx264",
                "-preset", "veryfast",
                "-crf", "23",
                "-c:a", "aac",
                "-b:a", "128k",
                output.toString()
        );

        pb.redirectErrorStream(true);
        Process p = pb.start();
        int exit = p.waitFor();
        if (exit != 0) throw new RuntimeException("ffmpeg failed with exit code " + exit);
    }
}
