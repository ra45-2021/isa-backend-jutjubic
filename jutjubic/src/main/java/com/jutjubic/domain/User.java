package com.jutjubic.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // korisničko ime
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    // lozinka (plain for now as you requested; later hash this)
    @Column(nullable = false, length = 200)
    private String password;

    // email adresa
    @Column(nullable = false, unique = true, length = 120)
    private String emailAdress;

    // ime
    @Column(nullable = false, length = 60)
    private String name;

    // prezime
    @Column(nullable = false, length = 60)
    private String surname;

    // adresa
    @Column(length = 200)
    private String adress;

    // bio (opis)
    @Column(length = 500)
    private String bio;

    // optional: URL to image (if null/empty -> frontend uses assets/profile.png)
    @Column(length = 500)
    private String profileImageUrl;

    @Column(nullable = false, length = 30)
    private String role = "USER";

    protected User() {}

    public Long getId() { return id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmailAdress() { return emailAdress; }
    public void setEmailAdress(String emailAdress) { this.emailAdress = emailAdress; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public String getAdress() { return adress; }
    public void setAdress(String adress) { this.adress = adress; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
