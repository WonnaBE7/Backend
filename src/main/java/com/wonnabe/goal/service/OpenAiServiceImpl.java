package com.wonnabe.goal.service;

import com.wonnabe.goal.dto.ChatRequestDTO;
import com.wonnabe.goal.dto.ChatResponseDTO;
import com.wonnabe.goal.dto.MessageDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class OpenAiServiceImpl implements OpenAiService {
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";

    @Value("${openai.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public String getGptResponse(String prompt) {
        // 1. HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        // 2. 요청 본문 생성
        MessageDTO systemMessage = new MessageDTO("system", "You are a helpful assistant who writes short, encouraging messages in Korean.");
        MessageDTO userMessage = new MessageDTO("user", prompt);
        ChatRequestDTO requestPayload = new ChatRequestDTO("gpt-4.1-nano-2025-04-14", List.of(systemMessage, userMessage));

        // 3. HTTP 요청 엔티티 생성
        HttpEntity<ChatRequestDTO> entity = new HttpEntity<>(requestPayload, headers);

        // 4. API 호출 및 응답 받기
        try {
            ChatResponseDTO response = restTemplate.postForObject(OPENAI_API_URL, entity, ChatResponseDTO.class);

            if (response != null && !response.getChoices().isEmpty()) {
                return response.getChoices().get(0).getMessage().getContent();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return null;
    }
}
