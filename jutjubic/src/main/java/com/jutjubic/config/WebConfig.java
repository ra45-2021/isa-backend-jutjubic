package com.jutjubic.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private UploadProperties uploadProperties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path baseDir = Paths.get(uploadProperties.getDir());
        if (!baseDir.isAbsolute()) {
            baseDir = Paths.get(System.getProperty("user.dir")).resolve(baseDir);
        }

        String location = baseDir.toAbsolutePath().normalize().toUri().toString();

        registry.addResourceHandler("/uploads/**", "/media/**")
                .addResourceLocations(location)
                .setCachePeriod(3600);
    }
}