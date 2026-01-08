package com.jutjubic.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Dobijamo apsolutnu putanju
        String rootPath = System.getProperty("user.dir");

        // Putanja do uploads foldera
        Path uploadsPath = Paths.get(rootPath, "uploads").toAbsolutePath().normalize();
        String location = uploadsPath.toUri().toString();

        // Mapiramo /uploads/** tako da gleda dubinski u sve podfoldere (thumbs, videos...)
        registry.addResourceHandler("/uploads/**", "/media/**")
                .addResourceLocations(location)
                .setCachePeriod(0);

        System.out.println("SERVER SERVING FROM: " + location);
    }
}
