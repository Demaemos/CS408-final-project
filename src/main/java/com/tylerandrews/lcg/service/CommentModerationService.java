package com.tylerandrews.lcg.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class CommentModerationService {

    private static final Pattern WORD_SEPARATOR = Pattern.compile("[^\\p{L}\\p{N}]+");
    private final List<List<String>> blockedTerms;

    public CommentModerationService(@Value("${lcg.comments.blocked-terms:}") String configuredTerms) {
        blockedTerms = Arrays.stream(configuredTerms.split(","))
            .map(String::trim)
            .filter(term -> !term.isEmpty())
            .map(CommentModerationService::tokens)
            .filter(term -> !term.isEmpty())
            .toList();
    }

    public boolean containsInappropriateLanguage(String body) {
        List<String> words = tokens(body == null ? "" : body);
        for (List<String> term : blockedTerms) {
            for (int start = 0; start <= words.size() - term.size(); start++) {
                if (words.subList(start, start + term.size()).equals(term)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static List<String> tokens(String value) {
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFKC)
            .toLowerCase(Locale.ROOT);
        return Arrays.stream(WORD_SEPARATOR.split(normalized))
            .filter(token -> !token.isEmpty())
            .toList();
    }
}
