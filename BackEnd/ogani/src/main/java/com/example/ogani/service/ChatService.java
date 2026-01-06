package com.example.ogani.service;

import com.example.ogani.entity.ChatMessage;
import com.example.ogani.entity.ChatSession;
import com.example.ogani.entity.User;
import com.example.ogani.exception.NotFoundException;
import com.example.ogani.repository.ChatMessageRepository;
import com.example.ogani.repository.ChatSessionRepository;
import com.example.ogani.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatService {

    @Autowired
    private ChatSessionRepository chatSessionRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public void deleteSession(Long sessionId, String username) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("Session not found"));

        if (!session.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized access to session");
        }

        chatMessageRepository.deleteBySession(session);
        chatSessionRepository.delete(session);
    }

    public ChatSession createSession(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        ChatSession session = new ChatSession();
        session.setUser(user);
        session.setCreatedAt(LocalDateTime.now());
        session.setTitle("New Chat " + LocalDateTime.now());
        return chatSessionRepository.save(session);
    }

    public List<ChatSession> getUserSessions(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return chatSessionRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public List<ChatMessage> getSessionMessages(Long sessionId, String username) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("Session not found"));
        
        if (!session.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized access to session");
        }
        
        return chatMessageRepository.findBySessionOrderByCreatedAtAsc(session);
    }

    public ChatMessage sendMessage(Long sessionId, String content, String username) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("Session not found"));

        if (!session.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized access to session");
        }

        if (chatMessageRepository.countBySession(session) == 0 && content != null && !content.trim().isEmpty()) {
            String title = content;
            if (title.length() > 100) {
                title = title.substring(0, 100) + "...";
            }
            session.setTitle(title);
            chatSessionRepository.save(session);
        }

        ChatMessage message = new ChatMessage();
        message.setSession(session);
        message.setSender("user");
        message.setContent(content);
        message.setCreatedAt(LocalDateTime.now());
        
        return chatMessageRepository.save(message);
    }

    public void appendMessages(Long sessionId, List<ChatMessage> messages, String username) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("Session not found"));

        if (!session.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized access to session");
        }

        if (chatMessageRepository.countBySession(session) == 0 && messages != null && !messages.isEmpty()) {
            String content = messages.get(0).getContent();
            if (content != null && !content.trim().isEmpty()) {
                String title = content;
                if (title.length() > 100) {
                    title = title.substring(0, 100) + "...";
                }
                session.setTitle(title);
                chatSessionRepository.save(session);
            }
        }

        for (ChatMessage msg : messages) {
            ChatMessage newMessage = new ChatMessage();
            newMessage.setSession(session);
            newMessage.setSender(msg.getSender());
            newMessage.setContent(msg.getContent());
            newMessage.setCreatedAt(LocalDateTime.now());
            chatMessageRepository.save(newMessage);
        }
    }
}
