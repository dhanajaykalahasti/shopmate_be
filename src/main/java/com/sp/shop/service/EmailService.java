package com.sp.shop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendRegistrationEmail(String toEmail, String username) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Registration Successful");
        message.setText("Dear " + username + ",\n\nYour registration was successful!\n\nThank you,\nTest Team");
        mailSender.send(message);
    }

    public void sendVerificationEmail(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Email Verification");
        message.setText("Your verification code is: " + code + "\n\nPlease use this code to verify your email address.");
        mailSender.send(message);
    }

    // Send registration success email
    public void sendSuccessEmail(String to, String username) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Email Verification Successful");
        message.setText("Dear " + username + ",\n\nYour email has been successfully verified. You can now log in to your account.");
        mailSender.send(message);
    }
}
