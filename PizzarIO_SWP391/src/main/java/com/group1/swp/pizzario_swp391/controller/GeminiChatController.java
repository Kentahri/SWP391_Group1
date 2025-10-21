package com.group1.swp.pizzario_swp391.controller;

import java.io.IOException;

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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
@Slf4j
public class GeminiChatController {

    private final GeminiChatService geminiChatService;

    /**
     * Endpoint chat thông thường
     */
    @PostMapping("/chat")
    public ResponseEntity<GeminiChatResponse> chat(@RequestBody GeminiChatRequest request) {
        
        try {
            if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
                return ResponseEntity.ok(
                    new GeminiChatResponse(null, false, "Message cannot be empty")
                );
            }
            String response = geminiChatService.chat(request.getMessage());
            return ResponseEntity.ok(
                new GeminiChatResponse(response, true, null)
            );

        } catch (Exception e) {
            return ResponseEntity.ok(
                new GeminiChatResponse(null, false, e.getMessage())
            );
        }
    }

    /**
     * Endpoint streaming - cho realtime response
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@RequestParam String message) {
        SseEmitter emitter = new SseEmitter(60000L); // 60 seconds timeout

        geminiChatService.streamChat(message, new GeminiChatService.StreamCallback() {
            @Override
            public void onChunk(String chunk) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("message")
                            .data(chunk));
                } catch (IOException e) {
                    emitter.completeWithError(e);
                }
            }

            @Override
            public void onComplete() {
                emitter.complete();
            }

            @Override
            public void onError(Exception e) {
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }
}

