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

    private record Rule(GeminiChatService.Intent intent, Pattern pattern) {
    }

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

    public GeminiChatService.Intent analyzerUserIntent(String normalized) {
        for (Rule rule : rules) {
            if (rule.pattern.matcher(normalized).find()) return rule.intent;
        }
        return GeminiChatService.Intent.OTHER;
    }
}
