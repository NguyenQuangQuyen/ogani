package com.example.ogani.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatbotResponse {
    
    private String message;          // Câu trả lời từ AI
    private boolean hasData;         // Có dữ liệu từ database hay không
    private String intent;           // Loại intent được nhận dạng
    
    public ChatbotResponse(String message) {
        this.message = message;
        this.hasData = false;
        this.intent = "GENERAL_QUESTION";
    }
}
