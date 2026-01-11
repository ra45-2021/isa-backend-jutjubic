package com.jutjubic.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginDto {
    @NotBlank(message = "Email adresa je obavezna")
    private String email;

    @NotBlank(message = "Lozinka je obavezna")
    private String password;

    public String getEmailAdress() { return email; }
    public void setEmailAdress(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}