package com.carmatch.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("CarMatch - Verify Your Email");
        message.setText(
                "Welcome to CarMatch!\n\n" +
                        "Your verification code is: " + otp + "\n\n" +
                        "This code expires in 10 minutes.\n\n" +
                        "If you didn't create a CarMatch account, please ignore this email."
        );
        mailSender.send(message);
    }
}