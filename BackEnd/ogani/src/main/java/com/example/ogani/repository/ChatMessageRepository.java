package com.example.ogani.repository;

import com.example.ogani.entity.ChatMessage;
import com.example.ogani.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findBySessionOrderByCreatedAtAsc(ChatSession session);
    long countBySession(ChatSession session);
    void deleteBySession(ChatSession session);
}
