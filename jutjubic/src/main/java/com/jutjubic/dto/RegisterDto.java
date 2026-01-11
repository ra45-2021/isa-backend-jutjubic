package com.jutjubic.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterDto {
    @NotBlank(message = "Email je obavezan")
    @Email(message = "Email format nije validan")
    private String email;

    @NotBlank(message = "Korisničko ime je obavezno")
    private String username;

    @NotBlank(message = "Lozinka je obavezna")
    @Size(min = 6, message = "Lozinka mora imati bar 6 karaktera")
    private String password;

    @NotBlank(message = "Ime je obavezno")
    private String name;

    @NotBlank(message = "Prezime je obavezno")
    private String surname;

    private String adress;
    private String phoneNumber;

    // Prazan konstruktor i Getteri/Setteri
    public RegisterDto() {}

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public String getAdress() { return adress; }
    public void setAdress(String adress) { this.adress = adress; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
}
