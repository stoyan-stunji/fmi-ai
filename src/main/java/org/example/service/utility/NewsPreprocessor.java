package org.example.service.utility;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class NewsPreprocessor {
    public List<String> preprocess(List<String> documents) {
        return documents.stream()
                .map(this::cleanText)
                .filter(d -> !isMostlyGarbage(d))
                .filter(d -> d.length() > 100)
                .collect(Collectors.toList());
    }

    private boolean isMostlyGarbage(String text) {
        if (text.length() < 50) {
            return true;
        }
        long alphaCount = text.chars().filter(Character::isLetter).count();
        double ratio = (double) alphaCount / text.length();
        long upperCount = text.chars().filter(Character::isUpperCase).count();
        double upperRatio = (double) upperCount / text.length();
        return ratio < 0.3 || upperRatio > 0.7;
    }

    private String cleanText(String text) {
        text = text.replaceAll("[^\\p{Print}\\n\\t]", " ");
        text = text.replaceAll("-{500,}|_{500,}|={500,}|\\*{500,}|~{500,}", " ");
        text = text.replaceAll("\\b(begin|start|file|name)\\s+[^\\s]{3,}\\.(zip|exe|rar)\\b", " ");
        text = text.replaceAll("\\b[A-Z0-9]+[^a-zA-Z0-9\\s]+[A-Z0-9]*\\b", " ");
        text = text.replaceAll("(?m)^•\\s*[^a-zA-Z0-9\\s]{2,}.*$", " ");
        text = text.replaceAll("\\bM[A-Z0-9][^a-zA-Z0-9\\s]{1,}", " ");
        text = text.replaceAll("\\b[A-Z0-9]{2,}[^a-zA-Z0-9\\s]+[A-Z0-9]{2,}\\b", " ");
        text = text.replaceAll("\\s*>[^a-zA-Z0-9\\s]+\\b", " ");
        text = text.replaceAll("\\b[^a-zA-Z0-9\\s]+<\\s*", " ");
        text = text.replaceAll("':76L\\$!", " ");
        text = text.replaceAll("\\[\\s*P\\s*0E[^a-zA-Z]+", " ");
        text = text.replaceAll("F/\\s*>\\^\\..+@P\\?&N\\[", " ");
        text = text.replaceAll("\\[[^\\]]*[^a-zA-Z0-9\\s]{3,}[^\\]]*\\]", " ");
        text = text.replaceAll("\\([^)]*[^a-zA-Z\\s]{3,}[^)]*\\)", " ");
        text = text.replaceAll("[\\[({]\\s*[])}]", " ");
        text = text.replaceAll("-+\\s*Part\\s+\\d+\\s+of\\s+\\d+\\s*-+", " ");
        text = text.replaceAll("\\[[^\\]]*\\]", " ");
        text = text.replaceAll("\\{[^}]*\\}", " ");
        text = text.replaceAll("//[^\\s]+", " ");
        text = text.replaceAll("(^|\\s)[^\\w\\s]+($|\\s)", " ");
        text = text.replaceAll("\\s+", " ").trim();

        text = Arrays.stream(text.split("\\n"))
                .filter(line -> {
                    int alpha = line.replaceAll("[^a-zA-Z]", "").length();
                    return alpha * 2 > line.length();
                })
                .collect(Collectors.joining("\n"));

        return text;
    }
}
