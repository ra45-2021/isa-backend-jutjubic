package com.jutjubic.dto;

public class UserDto {
    private Long id;
    private String username;
    private String emailAdress;
    private String name;
    private String surname;
    private String adress;
    private String bio;
    private String role;
    private String profileImageUrl;

    public UserDto(Long id, String username, String emailAdress, String name, String surname,
                   String adress, String bio, String role, String profileImageUrl) {
        this.id = id;
        this.username = username;
        this.emailAdress = emailAdress;
        this.name = name;
        this.surname = surname;
        this.adress = adress;
        this.bio = bio;
        this.role = role;
        this.profileImageUrl = profileImageUrl;
    }

    public UserDto(Long id, String username, String name, String surname, String profileImageUrl) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.surname = surname;
        this.profileImageUrl = profileImageUrl;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getEmailAdress() { return emailAdress; }
    public String getName() { return name; }
    public String getSurname() { return surname; }
    public String getAdress() { return adress; }
    public String getBio() { return bio; }
    public String getRole() { return role; }
    public String getProfileImageUrl() { return profileImageUrl; }
}
