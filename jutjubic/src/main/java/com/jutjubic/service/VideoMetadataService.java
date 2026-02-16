package com.jutjubic.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;

@Service
public class VideoMetadataService {

    /**
     * Extract video duration in seconds using ffprobe
     * Returns null if duration cannot be determined
     */
    public Double extractDuration(Path videoPath) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "ffprobe",
                    "-v", "error",
                    "-show_entries", "format=duration",
                    "-of", "default=noprint_wrappers=1:nokey=1",
                    videoPath.toAbsolutePath().toString()
            );

            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line = reader.readLine();
                if (line != null && !line.trim().isEmpty()) {
                    return Double.parseDouble(line.trim());
                }
            }

            process.waitFor();

        } catch (IOException | InterruptedException | NumberFormatException e) {
            System.err.println("WARNING: Could not extract video duration: " + e.getMessage());
            System.err.println("Make sure ffprobe is installed and accessible in PATH");
        }

        return null;
    }
}
