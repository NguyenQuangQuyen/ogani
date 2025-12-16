package com.example.ogani.service;

import com.example.ogani.model.request.AiRequest;
import com.example.ogani.model.response.AiResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
public class AiService {

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.model}")
    private String model;

    private final WebClient webClient;

    public AiService(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("https://generativelanguage.googleapis.com")
                .build();
    }

    public String generateContent(String prompt) {

        AiRequest request = new AiRequest(
                List.of(
                        new AiRequest.Content(
                                List.of(new AiRequest.Part(prompt))
                        )
                )
        );

        AiResponse response = webClient.post()
                .uri("/v1beta/models/{model}:generateContent?key={key}",
                        model, apiKey)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(AiResponse.class)
                .block();

        if (response == null
                || response.getCandidates() == null
                || response.getCandidates().isEmpty()) {
            return "";
        }

        return response.getCandidates()
                .get(0)
                .getContent()
                .getParts()
                .get(0)
                .getText();
    }
}
