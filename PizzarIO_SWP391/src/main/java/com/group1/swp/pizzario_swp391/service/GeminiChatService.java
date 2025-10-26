package com.group1.swp.pizzario_swp391.service;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.group1.swp.pizzario_swp391.entity.Product;
import com.group1.swp.pizzario_swp391.repository.ProductRepository;
import com.group1.swp.pizzario_swp391.utils.FuzzyIntentDetector;
import com.group1.swp.pizzario_swp391.utils.RegexIntentDetector;

@Service
@Slf4j
@RequiredArgsConstructor
public class GeminiChatService {

    private final Client geminiClient;
    private final FuzzyIntentDetector fuzzyIntentDetector;
    private final RegexIntentDetector regexIntentDetector;
    private final ProductRepository productRepository;

    @Value("${gemini.api.model}")
    private String model;

    public enum Intent {CHEAPEST, HIGHEST, PROMOTION, COMBO, BEST_SELLER, OTHER}

    @Component
    public interface SynonymProvider {
        Map<Intent, List<String>> getSynonyms();
    }

    private final class SynonymProviderImpl implements SynonymProvider {
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


    private final String systemPrompt = """
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
        regexIntentDetector.init(new SynonymProviderImpl());
        String raw = normalize(input);
        Intent regexIntent = regexIntentDetector.analyzerUserIntent(raw);
        if (regexIntent != Intent.OTHER) return regexIntent;
        return fuzzyIntentDetector.detect(raw);
    }


    public String chat(String userMessage) {
        try {
            Intent intent = detect(userMessage);
            log.info("Detected intent: {}", intent);

            switch (intent) {
                case CHEAPEST:
                    return handleCheapestIntent();

                case HIGHEST:
                    return handleHighestIntent();

                case PROMOTION:
                    return handlePromotionIntent();

                case COMBO:
                    return handleComboIntent();

                case BEST_SELLER:
                    return handleBestSellerIntent();

//                case OTHER:
                default:
                    return null;
            }

        } catch (Exception e) {
            log.error("Error in chat method: ", e);
            return "Xin lỗi, tôi đang gặp lỗi. Vui lòng thử lại sau! 😔";
        }
    }

    // ========== Query Methods (Placeholder - User will implement queries later) ==========

    private String handleCheapestIntent() {
        //  parameter kept for future use in context-aware responses
        try {
            List<Product> cheapestProducts = getCheapestProducts();
            if (cheapestProducts.isEmpty()) {
                return "Hiện tại không có sản phẩm nào trong menu. Vui lòng thử lại sau! 😊";
            }

            String response = buildProductResponse(cheapestProducts, "giá rẻ nhất");
            log.info("Cheapest products found: {}", cheapestProducts.size());
            return response;

        } catch (Exception e) {
            log.error("Error handling cheapest intent: ", e);
            return "Không thể tìm sản phẩm giá rẻ nhất. Vui lòng thử lại sau!";
        }
    }

    private String handleHighestIntent() {
        //  parameter kept for future use in context-aware responses
        try {
            List<Product> highestProducts = getHighestPriceProducts();
            if (highestProducts.isEmpty()) {
                return "Hiện tại không có sản phẩm nào trong menu. Vui lòng thử lại sau! 😊";
            }

            String response = buildProductResponse(highestProducts, "giá cao nhất");
            log.info("Highest products found: {}", highestProducts.size());
            return response;

        } catch (Exception e) {
            log.error("Error handling highest intent: ", e);
            return "Không thể tìm sản phẩm giá cao nhất. Vui lòng thử lại sau!";
        }
    }

    private String handlePromotionIntent() {
        //  parameter kept for future use in context-aware responses
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
        //  parameter kept for future use in context-aware responses
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
        //  parameter kept for future use in context-aware responses
        try {
            List<Product> bestSellerProducts = getBestSellerProducts();
            if (bestSellerProducts.isEmpty()) {
                return "Hiện tại không có sản phẩm nào trong menu. Vui lòng thử lại sau! 😊";
            }

            String response = buildProductResponse(bestSellerProducts, "bán chạy");
            log.info("Best seller products found: {}", bestSellerProducts.size());
            return response;

        } catch (Exception e) {
            log.error("Error handling best seller intent: ", e);
            return "Không thể tìm sản phẩm bán chạy. Vui lòng thử lại sau!";
        }
    }



    // ========== Database Query Methods (Placeholder - To be implemented) ==========

    /**
     * TODO: Implement query to get cheapest products
     * Query suggestion: ORDER BY base_price ASC, LIMIT to top 5-10 products
     */
    private List<Product> getCheapestProducts() {
        // Placeholder - User will implement the actual query
        return List.of(); // TODO: queryProductRepository().findCheapestProducts()
    }

    /**
     * TODO: Implement query to get highest price products
     * Query suggestion: ORDER BY base_price DESC, LIMIT to top 5-10 products
     */
    private List<Product> getHighestPriceProducts() {
        // Placeholder - User will implement the actual query
        return List.of(); // TODO: productRepository.findHighestPriceProducts()
    }

    /**
     * TODO: Implement query to get products with active promotions
     * Query suggestion: WHERE flash_sale_price IS NOT NULL
     *                   AND flash_sale_start <= NOW()
     *                   AND flash_sale_end >= NOW()
     *                   AND is_active = true
     */
    private List<Product> getPromotionProducts() {
        // Placeholder - User will implement the actual query
        List<Product> allProducts = productRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        return allProducts.stream()
                .filter(product -> product.getFlashSalePrice() > 0
                        && product.getFlashSaleStart() != null
                        && product.getFlashSaleEnd() != null
                        && !product.getFlashSaleStart().isAfter(now)
                        && !product.getFlashSaleEnd().isBefore(now)
                        && product.isActive())
                .collect(Collectors.toList());
    }

    /**
     * TODO: Implement query to get combo products
     * Query suggestion: JOIN with Category table
     *                   WHERE category.name LIKE '%combo%'
     *                   AND is_active = true
     */
    private List<Product> getComboProducts() {
        // Placeholder - User will implement the actual query
        List<Product> allProducts = productRepository.findAll();

        return allProducts.stream()
                .filter(product -> product.getCategory() != null
                        && product.getCategory().getName() != null
                        && product.getCategory().getName().toLowerCase().contains("combo")
                        && product.isActive())
                .collect(Collectors.toList());
    }

    /**
     * TODO: Implement query to get best seller products
     * Query suggestion: JOIN with OrderItem table
     *                   GROUP BY product_id
     *                   ORDER BY SUM(quantity) DESC
     *                   LIMIT to top 5-10 products
     */
    private List<Product> getBestSellerProducts() {
        // Placeholder - User will implement the actual query
        return List.of(); // TODO: Need to aggregate from OrderItem
    }

    // ========== Response Builder Methods ==========

    private String buildProductResponse(List<Product> products, String type) {
        if (products.isEmpty()) {
            return "Không tìm thấy sản phẩm nào phù hợp.";
        }

        StringBuilder response = new StringBuilder();

        if (products.size() == 1) {
            response.append("Đây là sản phẩm ").append(type).append(" của chúng tôi: ");
        } else {
            response.append("Đây là các sản phẩm ").append(type).append(" của chúng tôi: ");
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

        if (products.size() > 5) {
            response.append("\n\n... và còn ").append(products.size() - 5)
                    .append(" sản phẩm ").append(type).append(" khác!");
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

