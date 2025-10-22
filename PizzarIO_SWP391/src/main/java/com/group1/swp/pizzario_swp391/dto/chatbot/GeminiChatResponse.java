package com.group1.swp.pizzario_swp391.dto.chatbot;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeminiChatResponse {
    private String response;
    private boolean success;
    private String error;
}
