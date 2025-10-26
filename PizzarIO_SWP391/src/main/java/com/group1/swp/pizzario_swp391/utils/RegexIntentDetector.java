package com.group1.swp.pizzario_swp391.utils;

import com.group1.swp.pizzario_swp391.service.GeminiChatService;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.regex.Pattern;

/**
 *
 */
@Component
public class RegexIntentDetector {

    private record Rule(GeminiChatService.Intent intent, Pattern pattern) {}

    private List<Rule> rules;
    public void init(GeminiChatService.SynonymProvider synonymProvider) {
        this.rules = synonymProvider.getSynonyms().entrySet()
                .stream().map(entry ->
                        new Rule(entry.getKey(), Pattern.compile(toRegex(entry.getValue()))))
                .toList();
    }


    private static String toRegex(List<String> phrases) {
        return "\\b(" + String.join("|", phrases) + ")\\b";
    }


//    public static void main(String[] args) {
//        var detector = new RegexIntentDetector(() -> java.util.Map.of(
//                GeminiChatService.Intent.CHEAPEST, List.of("re nhat", "gia thap nhat", "re hon", "muc gia thap", "re nhat la"),
//                GeminiChatService.Intent.HIGHEST, List.of("dat nhat", "gia cao nhat", "dat hon", "dat nhat la"),
//                GeminiChatService.Intent.PROMOTION, List.of("khuyen mai", "uu dai", "giam gia", "deal", "voucher"),
//                GeminiChatService.Intent.BEST_SELLER, List.of("ban chay", "pho bien", "hot nhat", "nhieu nguoi mua", "yeu thich", "ngon nhat"),
//                GeminiChatService.Intent.COMBO, List.of("combo")
//        ));
//
//        detector.rules.forEach(rule ->
//            System.out.println("Intent: " + rule.intent + ", Pattern: " + rule.pattern)
//        );
//
//        String test = "ban dat nhat re nhat hien nay la mon gi?";
//        String normalized = test.toLowerCase().replaceAll("[^a-z0-9\\s]", " ").replaceAll("\\s+", " ").trim();
//        System.out.println("Normalized: " + normalized);
//        String intent = detector.analyzerUserIntent(normalized);
//        System.out.println("Detected intent: " + intent);
//    }

    public GeminiChatService.Intent analyzerUserIntent(String normalized) {
        for(Rule rule : rules){
            if(rule.pattern.matcher(normalized).find()) return rule.intent;
        }
        return GeminiChatService.Intent.OTHER;
    }
}
