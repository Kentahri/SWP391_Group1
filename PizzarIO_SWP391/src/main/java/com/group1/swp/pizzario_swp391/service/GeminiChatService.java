package com.group1.swp.pizzario_swp391.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class GeminiChatService {

    private final Client geminiClient;
    private final List<String> keyword = List.of();

    @Value("${gemini.api.model}")
    private String model;
//
//    @Value("${gemini.api.key}")
//    private String apiKey;

    public String chat(String userMessage) {
        try {
            String fullPrompt = """
                    Bạn là trợ lý ảo thông minh của nhà hàng pizza PizzarIO.
                    
                    Nhiệm vụ của bạn:
                    - Trả lời các câu hỏi về menu pizza, giá cả, khuyến mãi
                    - Tư vấn món ăn phù hợp
                    - Giải đáp thắc mắc về giờ mở cửa, địa chỉ, dịch vụ
                    
                    
                    
                    Phong cách: Thân thiện, ngắn gọn, dễ hiểu, sử dụng emoji phù hợp.
                    
                    Khách hàng hỏi: %s
                    """.formatted(userMessage);

            GenerateContentConfig config = GenerateContentConfig.builder()
                    .temperature(0.7f) // Độ sáng tạo, ngẫu nhiên của câu trả lời
                    .topK(40f) // Giới hạn số lựa chọn token
                    .topP(0.95f)  // Xác suất tích lũy
                    .maxOutputTokens(900)
                    .build();

            log.info("🚀 Calling Gemini API...");

            GenerateContentResponse response = geminiClient.models.generateContent(
                    model,
                    fullPrompt,
                    config
            );

            log.info("✅ Gemini API responded successfully!");
            log.info("📦 Response object: {}", response != null ? "NOT NULL" : "NULL");
            
            if (response != null) {
                String responseText = response.text();

                if (responseText != null && !responseText.isBlank()) {
                    return responseText;
                } else {
                    log.warn("⚠️ Response text is null or blank");
                }
            } else {
                log.error("❌ Response object is null!");
            }

            return "Xin lỗi, tôi không thể xử lý yêu cầu của bạn lúc này.";
            
        } catch (Exception e) {
            return "Đã có lỗi xảy ra. Vui lòng thử lại sau. Error: " + e.getMessage();
        }
    }

    public String chatWithContext(String userMessage, String conversationHistory) {
        try {
            String contextualPrompt = """
                Lịch sử hội thoại:
                %s
                
                Khách hàng hỏi: %s
                
                Hãy trả lời dựa trên ngữ cảnh hội thoại trước đó.
                """.formatted(conversationHistory, userMessage);

            return chat(contextualPrompt);

        } catch (Exception e) {
            log.error("Error in contextual chat: ", e);
            return "Đã có lỗi xảy ra khi xử lý hội thoại.";
        }
    }

    public void streamChat(String userMessage, StreamCallback callback) {
        try {
            GenerateContentConfig config = GenerateContentConfig.builder()
                    .temperature(0.7f)
                    .build();

            geminiClient.models.generateContentStream(
                    model,
                    userMessage,
                    config
            ).forEach(chunk -> {
                if (chunk.text() != null && !chunk.text().isBlank()) {
                    callback.onChunk(chunk.text());
                }

            });

            callback.onComplete();

        } catch (Exception e) {
            log.error("Error in streaming: ", e);
            callback.onError(e);
        }
    }

    @FunctionalInterface
    public interface StreamCallback {
        void onChunk(String chunk);

        default void onComplete() {}
        default void onError(Exception e) {}
    }


}
