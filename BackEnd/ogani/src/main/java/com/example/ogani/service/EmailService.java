package com.example.ogani.service;

import com.example.ogani.model.request.EmailRequest;

public interface EmailService {
    void sendContactEmail(EmailRequest emailRequest);
}
