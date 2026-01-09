package com.example.ogani.controller;

import com.example.ogani.model.response.ChatbotResponse;
import com.example.ogani.service.AiService;
import com.example.ogani.service.ChatbotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai-prompt")
public class AiPromptController {

    private final AiService aiService;
    private final ChatbotService chatbotService;

    public AiPromptController(AiService aiService, ChatbotService chatbotService) {
        this.aiService = aiService;
        this.chatbotService = chatbotService;
    }

    /**
     * Endpoint cũ - chỉ gọi AI không có database context
     * @deprecated Sử dụng /chat endpoint mới để có database integration
     */
    @PostMapping("/ask")
    public ResponseEntity<String> ask(@RequestBody String prompt) {
        return ResponseEntity.ok(aiService.generateContent(prompt));
    }

    /**
     * Endpoint mới - có khả năng truy vấn database
     */
    @PostMapping("/chat")
    public ResponseEntity<ChatbotResponse> chat(@RequestBody String prompt) {
        ChatbotResponse response = chatbotService.processQuery(prompt);
        return ResponseEntity.ok(response);
    }
}

