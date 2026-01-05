package com.jutjubic.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path root = Path.of(uploadDir).toAbsolutePath().normalize();

        /*System.out.println("=== WEB CONFIG LOADED ===");
        System.out.println("MEDIA ROOT DIR = " + root);
        System.out.println("MEDIA EXISTS = " + root.toFile().exists());
        System.out.println("MEDIA IS DIR = " + root.toFile().isDirectory());
        System.out.println("uploadDir property = " + uploadDir);*/


        registry.addResourceHandler("/media/**")
                .addResourceLocations("file:" + root.toString() + "/");
    }

}
