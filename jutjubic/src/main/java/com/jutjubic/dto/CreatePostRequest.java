package com.jutjubic.dto;

import org.springframework.web.multipart.MultipartFile;

public class CreatePostRequest {
    private String title;
    private String description;
    private String tags;
    private Double locationLat;
    private Double locationLon;
    private MultipartFile thumbnail;
    private MultipartFile video;

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public Double getLocationLat() { return locationLat; }
    public void setLocationLat(Double locationLat) { this.locationLat = locationLat; }

    public Double getLocationLon() { return locationLon; }
    public void setLocationLon(Double locationLon) { this.locationLon = locationLon; }

    public MultipartFile getThumbnail() { return thumbnail; }
    public void setThumbnail(MultipartFile thumbnail) { this.thumbnail = thumbnail; }

    public MultipartFile getVideo() { return video; }
    public void setVideo(MultipartFile video) { this.video = video; }
}