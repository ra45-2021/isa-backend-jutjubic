package com.jutjubic.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendActivationEmail(String to, String token) {
        String activationUrl = "http://localhost:8080/api/auth/activate?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Aktivacija naloga - Jutjubic");
        message.setText("Poštovani, \n\nKliknite na link ispod da biste aktivirali svoj nalog:\n" + activationUrl);

        mailSender.send(message);
    }
}
