package com.group1.swp.pizzario_swp391.controller;

import java.io.IOException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.group1.swp.pizzario_swp391.dto.chatbot.GeminiChatRequest;
import com.group1.swp.pizzario_swp391.dto.chatbot.GeminiChatResponse;
import com.group1.swp.pizzario_swp391.service.GeminiChatService;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
@Slf4j
public class GeminiChatController {

    private final GeminiChatService geminiChatService;

    @PostMapping(value = "/chat", produces = "application/json")
    public ResponseEntity<GeminiChatResponse> chat(@RequestBody GeminiChatRequest request) {
        log.info("========== CHAT ENDPOINT CALLED ==========");
        log.info("📨 Request: {}", request);
        
        try {
            if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
                log.warn("⚠️ Empty message");
                return ResponseEntity.ok(
                    new GeminiChatResponse(null, false, "Message cannot be empty")
                );
            }
            
            log.info("✅ Calling service...");
            String response = geminiChatService.chat(request.getMessage());
            
            log.info("✅ Service returned response");
            GeminiChatResponse chatResponse = new GeminiChatResponse(response, true, null);
            log.info("📦 Returning: {}", chatResponse);
            
            return ResponseEntity.ok(chatResponse);

        } catch (Exception e) {
            log.error("========== ERROR IN CHAT ENDPOINT ==========");
            log.error("❌ Exception: ", e);
            return ResponseEntity.badRequest()
                    .body(new GeminiChatResponse(null, false, e.getMessage()));
        }
    }

}

