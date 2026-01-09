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
        message.setSubject("Account Activation - Jutjubić");
        message.setText("Welcome to Jutjubić! \n\n\n Please click the link below to activate your account:\n" + activationUrl + "\n\n\n Thank you for using our services! \n\n\n Regards,\n Team 26");

        mailSender.send(message);
    }
}
