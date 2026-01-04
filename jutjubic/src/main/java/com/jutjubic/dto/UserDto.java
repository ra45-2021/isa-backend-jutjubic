package com.jutjubic.dto;

public class UserDto {

    private Long id;
    private String username;
    private String displayName;

    public UserDto(Long id, String username, String displayName) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }
}
