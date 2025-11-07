package com.group1.swp.pizzario_swp391.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.group1.swp.pizzario_swp391.entity.Product;
import com.group1.swp.pizzario_swp391.repository.OrderRepository;
import com.group1.swp.pizzario_swp391.repository.ProductRepository;
import com.group1.swp.pizzario_swp391.utils.FuzzyIntentDetector;
import com.group1.swp.pizzario_swp391.utils.RegexIntentDetector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class GeminiChatService {


    private final Client geminiClient;
    private final FuzzyIntentDetector fuzzyIntentDetector;
    private final RegexIntentDetector regexIntentDetector;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    @Value("${gemini.api.model}")
    private String model;

    public enum Intent {CHEAPEST, HIGHEST, PROMOTION, COMBO, BEST_SELLER, PIZZA, OTHER}


    public interface SynonymProvider {
        Map<Intent, List<String>> getSynonyms();
    }

    private static final class SynonymProviderImpl implements SynonymProvider {
        @Override
        public Map<Intent, List<String>> getSynonyms() {
            return Map.of(
                    Intent.PIZZA, List.of("pizza", "banh pizza", "mon pizza", "biza, piza"),
                    Intent.CHEAPEST, List.of("re nhat", "gia thap nhat", "re hon", "muc gia thap", "re nhat la"),
                    Intent.HIGHEST, List.of("dat nhat", "gia cao nhat", "dat hon", "dat nhat la"),
                    Intent.PROMOTION, List.of("khuyen mai", "uu dai", "giam gia", "deal", "voucher"),
                    Intent.BEST_SELLER, List.of("ban chay", "pho bien", "hot nhat", "nhieu nguoi mua", "yeu thich", "ngon nhat"),
                    Intent.COMBO, List.of("combo")
            );
        }
    }


    private static final String SYSTEM_PROMPT = """
            Bạn là chatbot tư vấn món ăn của nhà hàng PizzarIO 🍕✨
            Nhiệm vụ của bạn là giúp khách chọn món một cách dễ dàng, nhanh chóng và vui vẻ.
            
            Phong cách trả lời:
            - Dễ thương, nhiệt tình, thân thiện 😄
            - Câu trả lời ngắn gọn, rõ ràng, dễ đọc (không dùng câu dài).
            - Khuyến khích, gợi ý, không ép buộc.
            - Dùng emoji hợp lý (1–3 emoji mỗi câu, đừng lạm dụng).
            - Nếu người dùng hỏi chung chung → hỏi lại để làm rõ nhu cầu (ví dụ: ăn mấy người? thích vị gì?).
            - Không dùng các cái ký hiệu trong markdown như **, *.
            
            Khi tư vấn món:
            - Nếu khách hỏi món rẻ nhất → gợi ý các món giá thấp dễ chọn.
            - Nếu khách hỏi món đắt / cao cấp → gợi ý món .
            - Nếu khách hỏi khuyến mãi → ưu tiên món đang flash sale hoặc voucher.
            - Nếu khách hỏi combo → gợi ý combo kèm nước/khai vị cho tiện.
            - Nếu khách hỏi bán chạy → giới thiệu các món bán được nhiều.
            --> Tất cả sẽ dựa trên dữ liệu về món sẽ được đính kèm ở bên dưới, chứ không tự bịa ra.
            
            Không làm:
            - Không trả lời như robot.
            - Không nói lan man dài dòng.
            - Không nhắc đến mô hình AI hay cách bạn được tạo ra.
            
            Cuối mỗi câu trả lời:
            - Gợi ý hành động tiếp theo cho khách (ví dụ: hỏi thêm về số người ăn, gợi ý món khác, hỏi về sở thích vị ăn, v.v.).
            - Gợi ý khách xem thêm món ở trn màn hình để biết thêm chi tiết nhé
            
            Ví dụ câu trả lời chuẩn:
            “Bạn muốn tìm món giá dễ thương đúng không nè? 😄 \s
            Mình đề xuất thử Pizza Hải Sản Mini và Pizza Bò Phô Mai size S, vừa ngon vừa tiết kiệm 💛 \s
            Bạn ăn mấy người để mình gợi ý chuẩn hơn nha? 😋”
            
            Dưới đây là dữ liệu món ăn được cung cấp dựa trên câu hỏi của người dùng:
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
        log.debug("Intent for input: {}", input);
        SynonymProviderImpl provider = new SynonymProviderImpl();
        regexIntentDetector.init(provider);
        String raw = normalize(input);
//        log.debug("📝 Normalized input: {}", raw);

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
            Intent intent = detect(userMessage);
            log.info("✅ Detected intent: {}", intent);

            String requirement;
            switch (intent) {
                case CHEAPEST -> {
                    requirement = handleCheapestIntent();
                }
                case HIGHEST -> {
                    requirement = handleHighestIntent();
                }
                case PROMOTION -> {
                    requirement = handlePromotionIntent();
                }
                case COMBO -> {
                    requirement = handleComboIntent();
                }
                case BEST_SELLER -> {
                    requirement = handleBestSellerIntent();
                }
                case PIZZA -> {
                    requirement = handlePizzaIntent();
                }
                default -> {
                    requirement = "Bạn có thể cung cấp thêm thông tin để mình giúp bạn chọn món phù hợp hơn không? 😊";
                }
            }

            requirement = requirement + "\n\nKhách hàng hỏi: " + userMessage;
            String fullPrompt = SYSTEM_PROMPT + "\n" + requirement;
            log.info("🤖 Calling Gemini API with model: {}", model);

            GenerateContentConfig config = GenerateContentConfig.builder()
                    .temperature(0.7f)
                    .topK(50f)
                    .topP(0.85f)
                    .maxOutputTokens(4096)
                    .build();

            return geminiClient.models.generateContent(model, fullPrompt, config).text();
        } catch (Exception e) {
            log.error("❌ Error in chat method: ", e);
            return "Xin lỗi, tôi đang gặp lỗi. Vui lòng thử lại sau! 😔";
        }
    }

    private String handlePizzaIntent() {
        try {
            List<Product> pizzaProducts = getPizzaProducts();
            if (pizzaProducts.isEmpty()) {
                return "Hiện tại không có sản phẩm pizza trong menu. Vui lòng xem các sản phẩm khác nhé! 🍕";
            }

            return buildProductResponse(pizzaProducts, "others");
        } catch (Exception e) {
            return "Không thể tìm sản phẩm pizza. Vui lòng thử lại sau!";
        }
    }

    private String handleCheapestIntent() {
        try {
            log.info("🔍 Fetching cheapest products...");
            List<Product> cheapestProducts = getCheapestProducts();
            if (cheapestProducts.isEmpty()) {
                return "Hiện tại không có sản phẩm giá thấp trong menu. Vui lòng xem các sản phẩm khác nhé! 💎";
            }
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
            if (highestProducts.isEmpty()) {
                return "Hiện tại không có sản phẩm cao cấp trong menu. Vui lòng xem các sản phẩm khác nhé! 💎";
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
            if (bestSellerProducts.isEmpty()) {
                return "Hiện tại chưa có sản phẩm bán chạy. Vui lòng xem các sản phẩm trong menu nhé! 🔥";
            }
            String response = buildProductResponse(bestSellerProducts, "bán chạy");
            log.info("Best seller products found: {}", bestSellerProducts.size());
            return response;

        } catch (Exception e) {
            log.error("Error handling best seller intent: ", e);
            return "Không thể tìm sản phẩm bán chạy. Vui lòng thử lại sau!";
        }
    }

    private List<Product> getPizzaProducts() {
        return productRepository.findByCategoryNameContainingIgnoreCaseAndActiveTrue("Pizza");
    }


    private List<Product> getCheapestProducts() {
        Pageable pageable = PageRequest.of(0, 5);
        return productRepository.findCheapestProducts(pageable);
    }


    private List<Product> getHighestPriceProducts() {
        Pageable pageable = PageRequest.of(0, 5);
        return productRepository.findHighestPriceProducts(pageable);
    }

    private List<Product> getPromotionProducts() {
        return productRepository.findPromotionProducts();
    }


    private List<Product> getComboProducts() {
        return productRepository.findByCategoryNameContainingIgnoreCaseAndActiveTrue("Combo");

    }

    private List<Product> getBestSellerProducts() {
        return orderRepository.findTopBestSellingProductsForGemini(5);
    }


    private String buildProductResponse(List<Product> products, String type) {
        StringBuilder response = new StringBuilder();

        switch (type.toLowerCase()) {
            case "giá rẻ nhất" -> response.append("Các món giá rẻ nhất:\n\n");
            case "giá cao nhất" -> response.append("Các món cao cấp:\n\n");
            case "khuyến mãi" -> response.append("Các món đang khuyến mãi:\n\n");
            case "combo" -> response.append("Combo:\n\n");
            case "bán chạy" -> response.append("Món bán chạy nhất:\n\n");
            case "pizza" -> response.append("Các món pizza:\n\n");
            default -> response.append("Danh sách sản phẩm:\n\n");
        }

        for (int i = 0; i < Math.min(products.size(), 5); i++) {
            Product product = products.get(i);
            response.append(i + 1).append(". ").append(product.getName());


            if (product.getProductSizes() != null && !product.getProductSizes().isEmpty()) {
                response.append("\n   ");
                for (int j = 0; j < product.getProductSizes().size(); j++) {
                    var productSize = product.getProductSizes().get(j);
                    if (j > 0) {
                        response.append(" | ");
                    }
                    response.append("Size ").append(productSize.getSize().getSizeName()).append(": ");

                    if (productSize.isOnFlashSale()) {
                        response.append(formatPrice(productSize.getFlashSalePrice()))
                                .append(" (giảm từ ").append(formatPrice(productSize.getBasePrice())).append(" xuống)");
                    } else {
                        response.append(formatPrice(productSize.getBasePrice()));
                    }
                }
            }
            response.append("\n\n");
        }

        System.out.println(response.toString());

        return response.toString();
    }

    private String formatPrice(double price) {
        return String.format("%,.0f VNĐ", price);
    }

}

