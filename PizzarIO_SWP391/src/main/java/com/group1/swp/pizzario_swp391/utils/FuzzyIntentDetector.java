package com.group1.swp.pizzario_swp391.utils;

import java.util.List;
import java.util.Map;

import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.springframework.stereotype.Component;

import com.group1.swp.pizzario_swp391.service.GeminiChatService;

@Component
public class FuzzyIntentDetector {

    private static final double THRESHOLD = 0.8;

    public record Result(GeminiChatService.Intent intent, double score) {
    }

    private Map<GeminiChatService.Intent, List<String>> synonyms;
    private static final JaroWinklerSimilarity jaroWinklerSimilarity = new JaroWinklerSimilarity();

    public void init(GeminiChatService.SynonymProvider synonymProvider) {
        this.synonyms = synonymProvider.getSynonyms();
    }

    public GeminiChatService.Intent detect(String input) {
        if (input == null || input.isBlank() || synonyms == null) return GeminiChatService.Intent.OTHER;

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
