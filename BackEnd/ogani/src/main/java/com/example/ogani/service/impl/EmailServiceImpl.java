package com.example.ogani.service.impl;

import com.example.ogani.model.request.EmailRequest;
import com.example.ogani.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender emailSender;

    @Override
    public void sendContactEmail(EmailRequest emailRequest) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("quyenquy053@gmail.com");
        message.setSubject("Liên hệ mới từ khách hàng: " + emailRequest.getName());
        message.setText("Tên khách hàng: " + emailRequest.getName() + "\n" +
                        "Email: " + emailRequest.getEmail() + "\n\n" +
                        "Nội dung tin nhắn:\n" + emailRequest.getMessage());
        
        emailSender.send(message);
    }
}
