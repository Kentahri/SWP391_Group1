package com.group1.swp.pizzario_swp391.utils;

import com.group1.swp.pizzario_swp391.service.GeminiChatService;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class FuzzyIntentDetector {

    private static final double THRESHOLD = 0.8;

    public record Result(GeminiChatService.Intent intent, double score) {
    }

    private final Map<GeminiChatService.Intent, List<String>> synonyms;
    private final JaroWinklerSimilarity jaroWinklerSimilarity = new JaroWinklerSimilarity();

    public FuzzyIntentDetector(GeminiChatService.SynonymProvider synonymProvider) {
        this.synonyms = synonymProvider.getSynonyms();
    }

//    public static void main(String[] args) {
//        var detector = new FuzzyIntentDetector(() -> Map.of(
//                GeminiChatService.Intent.CHEAPEST, List.of("re nhat", "gia thap nhat", "re hon", "muc gia thap", "re nhat la"),
//                GeminiChatService.Intent.HIGHEST, List.of("dat nhat", "gia cao nhat", "dat hon", "dat nhat la"),
//                GeminiChatService.Intent.PROMOTION, List.of("khuyen mai", "uu dai", "giam gia", "deal", "voucher"),
//                GeminiChatService.Intent.BEST_SELLER, List.of("ban chay", "pho bien", "hot nhat", "nhieu nguoi mua", "yeu thich", "ngon nhat"),
//                GeminiChatService.Intent.COMBO, List.of("combo")
//        ));
//
//        String test = "mon an re nhat hien nay la gi?";
//        Result result = detector.detect(test);
//        System.out.println("Detected intent: " + result.intent + ", score: " + result.score);
//
//        System.out.println(detector.slidingWindowSimilarity("mon re an nhatedsa hien nay la gi?", "re nhat"));
//        System.out.println(detector.slidingWindowSimilarity("mon an datnhat hien nay la gi?", "re nhat"));
//        System.out.println(detector.slidingWindowSimilarity("mon an hehe hien nay la gi?", "re nhat"));
//
//    }

    public GeminiChatService.Intent detect(String input) {
        if (input == null || input.isBlank()) return GeminiChatService.Intent.OTHER;

        GeminiChatService.Intent bestIntent = GeminiChatService.Intent.OTHER;
        double bestScore = 0.0;

        for (var entry : synonyms.entrySet()) {
            for (var phrase : entry.getValue()) {
                double score = slidingWindowSimilarity(input, phrase);
                if (score > bestScore) {
                    bestScore = score;
                    bestIntent = entry.getKey();
                }
            }
        }

        var intent = bestScore >= THRESHOLD ?
                new Result(bestIntent, bestScore) :
                new Result(GeminiChatService.Intent.OTHER, bestScore);
        return intent.intent;
    }

    /**
     * Dùng thuật toán sliding window kết hợp Jaro-Winkler để tính độ tương đồng giữa chuỗi input và target
     */
    private double slidingWindowSimilarity(String input, String target) {
        double best = jaroWinklerSimilarity.apply(input, target);
        int targetLength = target.length();
        if (input.length() > targetLength) {
            for (int i = 0; i + targetLength < input.length(); i++) {
                double score = jaroWinklerSimilarity.apply(input.substring(i, i + targetLength), target);
                if (score > best) best = score;
            }
        }
        return best;
    }
}
