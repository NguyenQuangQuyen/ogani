package com.example.ogani.controller;

import com.example.ogani.entity.ChatMessage;
import com.example.ogani.entity.ChatSession;
import com.example.ogani.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    @PostMapping("/sessions")
    public ResponseEntity<ChatSession> createSession() {
        String username = getCurrentUsername();
        return ResponseEntity.ok(chatService.createSession(username));
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<ChatSession>> getUserSessions() {
        String username = getCurrentUsername();
        return ResponseEntity.ok(chatService.getUserSessions(username));
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<List<ChatMessage>> getSessionMessages(@PathVariable Long sessionId) {
        String username = getCurrentUsername();
        return ResponseEntity.ok(chatService.getSessionMessages(sessionId, username));
    }

    @PostMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<ChatMessage> sendMessage(@PathVariable Long sessionId, @RequestBody Map<String, String> payload) {
        String content = payload.get("message");
        String username = getCurrentUsername();
        return ResponseEntity.ok(chatService.sendMessage(sessionId, content, username));
    }

    @PostMapping("/sessions/{sessionId}/append")
    public ResponseEntity<Void> appendMessages(@PathVariable Long sessionId, @RequestBody List<ChatMessage> messages) {
        String username = getCurrentUsername();
        chatService.appendMessages(sessionId, messages, username);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Void> deleteSession(@PathVariable Long sessionId) {
        String username = getCurrentUsername();
        chatService.deleteSession(sessionId, username);
        return ResponseEntity.ok().build();
    }
}
