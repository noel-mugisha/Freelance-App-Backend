package com.demo.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }


    public void sendVerificationOtp(String to, String otp) {
        String subject = "Your Verification Code";
        String body = "Thank you for registering! Your One-Time Password (OTP) for email verification is:\n\n"
                + ">> " + otp + " <<\n\n"
                + "This code will expire in 10 minutes.";

        sendEmail(to, subject, body);
    }

    public void sendPasswordResetOtp(String to, String otp) {
        String subject = "Your Password Reset Code";
        String body = "You requested a password reset. Your One-Time Password (OTP) is:\n\n"
                + ">> " + otp + " <<\n\n"
                + "This code will expire in 10 minutes. If you did not request this, please ignore this email.";

        sendEmail(to, subject, body);
    }

    private void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception e) {
            // In a production environment, you should use a logging framework
            System.err.println("Error sending email: " + e.getMessage());
            // Consider throwing a custom exception or using a queue for retries
        }
    }
}