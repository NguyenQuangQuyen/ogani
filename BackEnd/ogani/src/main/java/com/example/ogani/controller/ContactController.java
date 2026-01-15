package com.example.ogani.controller;

import com.example.ogani.model.request.EmailRequest;
import com.example.ogani.model.response.MessageResponse;
import com.example.ogani.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contact")
@CrossOrigin(origins = "https://hgr0a62zxby.sn.mynetname.net:1411")
public class ContactController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/send")
    public ResponseEntity<MessageResponse> sendContactMessage(@RequestBody EmailRequest emailRequest) {
        try {
            emailService.sendContactEmail(emailRequest);
            return ResponseEntity.ok(new MessageResponse("Tin nhắn đã được gửi thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Gửi tin nhắn thất bại: " + e.getMessage()));
        }
    }
}
