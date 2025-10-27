package com.group1.swp.pizzario_swp391.service;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.group1.swp.pizzario_swp391.entity.Product;
import com.group1.swp.pizzario_swp391.repository.OrderRepository;
import com.group1.swp.pizzario_swp391.repository.ProductRepository;
import com.group1.swp.pizzario_swp391.utils.FuzzyIntentDetector;
import com.group1.swp.pizzario_swp391.utils.RegexIntentDetector;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiChatService {


    private final Client geminiClient;
    private final FuzzyIntentDetector fuzzyIntentDetector;
    private final RegexIntentDetector regexIntentDetector;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    @Value("${gemini.api.model}")
    private String model;

    public enum Intent {CHEAPEST, HIGHEST, PROMOTION, COMBO, BEST_SELLER, OTHER}

    public interface SynonymProvider {
        Map<Intent, List<String>> getSynonyms();
    }

    private static final class SynonymProviderImpl implements SynonymProvider {
        @Override
        public Map<Intent, List<String>> getSynonyms() {
            return Map.of(
                    Intent.CHEAPEST, List.of("re nhat", "gia thap nhat", "re hon", "muc gia thap", "re nhat la"),
                    Intent.HIGHEST, List.of("dat nhat", "gia cao nhat", "dat hon", "dat nhat la"),
                    Intent.PROMOTION, List.of("khuyen mai", "uu dai", "giam gia", "deal", "voucher"),
                    Intent.BEST_SELLER, List.of("ban chay", "pho bien", "hot nhat", "nhieu nguoi mua", "yeu thich", "ngon nhat"),
                    Intent.COMBO, List.of("combo")
            );
        }
    }


    private static final String SYSTEM_PROMPT = """
            Bạn là trợ lý ảo thông minh của nhà hàng pizza PizzarIO.
            
            Nhiệm vụ của bạn:
            - Trả lời các câu hỏi về menu pizza, giá cả, khuyến mãi
            - Tư vấn món ăn phù hợp
            - Giải đáp thắc mắc về giờ mở cửa, địa chỉ, dịch vụ
            
            
            
            Phong cách: Thân thiện, ngắn gọn, dễ hiểu, sử dụng emoji phù hợp.
            """;

    private String normalize(String input) {
        if (input == null || input.isBlank()) return "";
        String normalized = Normalizer.normalize(input.toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        if (normalized.contains("đ")) {
            normalized = normalized.replace("đ", "d");
        }
        return normalized.replaceAll("[^a-z0-9\\s%]", " ").replaceAll("\\s+", " ").trim();
    }

    private Intent detect(String input) {
        log.debug("🔍 Detecting intent for input: {}", input);
        SynonymProviderImpl provider = new SynonymProviderImpl();
        regexIntentDetector.init(provider);
        String raw = normalize(input);
        log.debug("📝 Normalized input: {}", raw);
        
        Intent regexIntent = regexIntentDetector.analyzerUserIntent(raw);
        log.debug("🎯 Regex intent: {}", regexIntent);
        
        if (regexIntent != Intent.OTHER) {
            log.info("✅ Intent detected by regex: {}", regexIntent);
            return regexIntent;
        }
        
        fuzzyIntentDetector.init(provider);
        Intent fuzzyIntent = fuzzyIntentDetector.detect(raw);
        log.info("✅ Intent detected by fuzzy: {}", fuzzyIntent);
        return fuzzyIntent;
    }


    public String chat(String userMessage) {
        try {
            log.info("🔍 Starting chat with message: {}", userMessage);
            Intent intent = detect(userMessage);
            log.info("✅ Detected intent: {}", intent);
            
            StringBuilder fullPrompt;
            String requirement;
            switch (intent) {
                case CHEAPEST -> {
                    log.info("💰 Handling CHEAPEST intent...");
                    requirement = handleCheapestIntent();
                }
                case HIGHEST -> {
                    log.info("💎 Handling HIGHEST intent...");
                    requirement = handleHighestIntent();
                }
                case PROMOTION -> {
                    log.info("🎉 Handling PROMOTION intent...");
                    requirement = handlePromotionIntent();
                }
                case COMBO -> {
                    log.info("🍕 Handling COMBO intent...");
                    requirement = handleComboIntent();
                }
                case BEST_SELLER -> {
                    log.info("🔥 Handling BEST_SELLER intent...");
                    requirement = handleBestSellerIntent();
                }
                default -> {
                    log.info("❓ Handling OTHER intent (general chat)...");
                    requirement = "";
                }
            }

            log.info("📝 Requirement generated, length: {}", requirement.length());
            fullPrompt = new StringBuilder(SYSTEM_PROMPT).append("\n").append(requirement);
            log.info("🤖 Calling Gemini API with model: {}", model);
            
            GenerateContentConfig config = GenerateContentConfig.builder()
                    .temperature(0.7f)
                    .topK(50f)
                    .topP(0.85f)
                    .maxOutputTokens(2000)
                    .build();

            String response = geminiClient.models.generateContent(model, fullPrompt.toString(), config).text();
            log.info("✅ Got response from Gemini, length: {}", response.length());
            return response;
        } catch (Exception e) {
            log.error("❌ Error in chat method: ", e);
            return "Xin lỗi, tôi đang gặp lỗi. Vui lòng thử lại sau! 😔";
        }
    }


    private String handleCheapestIntent() {
        try {
            log.info("🔍 Fetching cheapest products...");
            List<Product> cheapestProducts = getCheapestProducts();
            log.info("✅ Found {} cheapest products", cheapestProducts.size());

            String response = buildProductResponse(cheapestProducts, "giá rẻ nhất");
            log.info("📝 Built response for cheapest products");
            return response;
        } catch (Exception e) {
            log.error("❌ Error handling cheapest intent: ", e);
            return "Không thể tìm sản phẩm giá rẻ nhất. Vui lòng thử lại sau!";
        }
    }

    private String handleHighestIntent() {
        try {
            List<Product> highestProducts = getHighestPriceProducts();

            String response = buildProductResponse(highestProducts, "giá cao nhất");
            log.info("Highest products found: {}", highestProducts.size());
            return response;

        } catch (Exception e) {
            log.error("Error handling highest intent: ", e);
            return "Không thể tìm sản phẩm giá cao nhất. Vui lòng thử lại sau!";
        }
    }

    private String handlePromotionIntent() {
        try {
            List<Product> promotionProducts = getPromotionProducts();
            if (promotionProducts.isEmpty()) {
                return "Hiện tại không có sản phẩm đang khuyến mãi. Vui lòng quay lại sau để không bỏ lỡ ưu đãi! 🎉";
            }

            String response = buildProductResponse(promotionProducts, "khuyến mãi");
            log.info("Promotion products found: {}", promotionProducts.size());
            return response;

        } catch (Exception e) {
            log.error("Error handling promotion intent: ", e);
            return "Không thể tìm sản phẩm khuyến mãi. Vui lòng thử lại sau!";
        }
    }

    private String handleComboIntent() {
        try {
            List<Product> comboProducts = getComboProducts();
            if (comboProducts.isEmpty()) {
                return "Hiện tại không có combo nào trong menu. Vui lòng xem các sản phẩm khác nhé! 🍕";
            }

            String response = buildProductResponse(comboProducts, "combo");
            log.info("Combo products found: {}", comboProducts.size());
            return response;

        } catch (Exception e) {
            log.error("Error handling combo intent: ", e);
            return "Không thể tìm combo. Vui lòng thử lại sau!";
        }
    }

    private String handleBestSellerIntent() {
        try {
            List<Product> bestSellerProducts = getBestSellerProducts();

            String response = buildProductResponse(bestSellerProducts, "bán chạy");
            log.info("Best seller products found: {}", bestSellerProducts.size());
            return response;

        } catch (Exception e) {
            log.error("Error handling best seller intent: ", e);
            return "Không thể tìm sản phẩm bán chạy. Vui lòng thử lại sau!";
        }
    }


    private List<Product> getCheapestProducts() {
        log.debug("📦 Querying database for cheapest products...");
        List<Product> products = productRepository.findCheapestProducts();
        log.debug("✅ Retrieved {} products from database", products.size());
        return products;
    }


    private List<Product> getHighestPriceProducts() {
        return productRepository.findHighestPriceProducts();
    }

    private List<Product> getPromotionProducts() {
        List<Product> allProducts = productRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        return allProducts.stream()
                .filter(product -> product.getFlashSalePrice() > 0
                        && product.getFlashSaleStart() != null
                        && product.getFlashSaleEnd() != null
                        && !product.getFlashSaleStart().isAfter(now)
                        && !product.getFlashSaleEnd().isBefore(now)
                        && product.isActive())
                .toList();
    }


    private List<Product> getComboProducts() {
        List<Product> allProducts = productRepository.findAll();

        return allProducts.stream()
                .filter(product -> product.getCategory() != null
                        && product.getCategory().getName() != null
                        && product.getCategory().getName().toLowerCase().contains("combo")
                        && product.isActive())
                .toList();
    }

    private List<Product> getBestSellerProducts() {
        return orderRepository.findTopBestSellingProductsForGemini(5);
    }


    private String buildProductResponse(List<Product> products, String type) {
        StringBuilder response = new StringBuilder();
        
        switch (type.toLowerCase()) {
            case "giá rẻ nhất" -> response.append("Các món giá rẻ nhất:");
            case "giá cao nhất" -> response.append("Các món cao cấp:");
            case "khuyến mãi" -> response.append("Các món đang khuyến mãi:");
            case "combo" -> response.append("Combo:");
            case "bán chạy" -> response.append("Món bán chạy nhất:");
        }

        for (int i = 0; i < Math.min(products.size(), 5); i++) {
            Product product = products.get(i);
            response.append("\n\n🍕 ").append(product.getName());

            if (product.getBasePrice() > 0) {
                response.append(" - ").append(formatPrice(product.getBasePrice()));
            }

            if (product.getDescription() != null && !product.getDescription().isEmpty()) {
                response.append("\n   ").append(product.getDescription());
            }
        }

        return response.toString();
    }

    private String formatPrice(double price) {
        return String.format("%,.0f VNĐ", price);
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

        default void onComplete() {
        }

        default void onError(Exception e) {
        }
    }
}

