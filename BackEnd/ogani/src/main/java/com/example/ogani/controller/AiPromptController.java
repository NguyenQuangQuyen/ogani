package com.example.ogani.controller;

import com.example.ogani.service.AiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai-prompt")
public class AiPromptController {

    private final AiService aiService;

    public AiPromptController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/ask")
    public ResponseEntity<String> ask(@RequestBody String prompt) {
        return ResponseEntity.ok(aiService.generateContent(prompt));
    }
}
