package org.example.datastructures.summarizers;

import org.example.datastructures.AbstractSummarizer;

import java.util.*;

public class TwentyNewsGroupsSummarizer implements AbstractSummarizer {
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "a", "able", "about", "above", "after", "again", "against", "ain", "all", "also", "am", "an", "and", "any",
            "aren", "are", "aren't", "as", "at", "be", "because", "been", "before", "being", "below", "between", "both",
            "but", "by", "can", "cannot", "could", "couldn", "couldn't", "did", "didn", "didn't", "do", "does", "doesn",
            "doesn't", "doing", "don", "don't", "down", "during", "each", "either", "else", "ever", "every", "few", "for",
            "from", "further", "get", "got", "had", "hadn", "hadn't", "has", "hasn", "hasn't", "have", "haven", "haven't",
            "having", "he", "her", "here", "hers", "herself", "him", "himself", "his", "how", "however", "i", "if", "in",
            "into", "is", "isn", "isn't", "it", "it's", "its", "itself", "just", "let", "like", "likely", "may", "might",
            "mightn", "mightn't", "mine", "more", "most", "must", "mustn", "mustn't", "my", "myself", "neither", "no",
            "nor", "not", "now", "of", "off", "often", "on", "once", "only", "or", "other", "ought", "our", "ours",
            "ourselves", "out", "over", "own", "same", "s", "said", "say", "says", "she", "she's", "should", "should've",
            "shouldn", "shouldn't", "so", "some", "such", "t", "than", "that", "that'll", "the", "their", "theirs",
            "them", "themselves", "then", "there", "these", "they", "they'd", "they'll", "they're", "they've", "this",
            "those", "through", "to", "too", "under", "until", "up", "us", "very", "was", "wasn", "wasn't", "we", "were",
            "weren", "weren't", "what", "when", "where", "which", "while", "who", "whom", "why", "will", "with", "won",
            "won't", "would", "wouldn", "wouldn't", "y", "you", "you'd", "you'll", "you're", "you've", "your", "yours",
            "yourself", "yourselves"
    ));

    @Override
    public String summarize(String text, int maxSummarySize) {
        if (text == null || text.trim().isEmpty()) {
            return "Nothing to summarize...";
        }

        String[] sentences = getSentences(text);
        if (sentences.length == 0) {
            return "";
        }

        List<String> importantWords = extractImportantWords(text);
        List<String> summarySentences = selectSummarySentences(importantWords, sentences, maxSummarySize);
        return formatSummary(sentences, summarySentences);
    }

    private List<String> extractImportantWords(String text) {
        Map<String, Integer> wordFrequencies = filterStopWords(getWordCounts(text));
        return sortByFreqThenDropFreq(wordFrequencies);
    }

    private List<String> selectSummarySentences(List<String> words, String[] sentences, int maxSize) {
        List<String> selected = new ArrayList<>();
        selected.add(sentences[0]);
        for (String word : words) {
            String match = search(sentences, word);
            if (match != null && !selected.contains(match)) {
                selected.add(match);
            }
            if (selected.size() >= maxSize) {
                break;
            }
        }
        return selected;
    }

    private String formatSummary(String[] sentences, List<String> summarySentences) {
        StringBuilder summary = new StringBuilder();
        for (String sentence : sentences) {
            if (summarySentences.contains(sentence)) {
                String[] words = sentence.trim().split("\\s+");
                summary.append("\t");
                for (int i = 0; i < words.length; i++) {
                    summary.append(words[i]).append(" ");
                    if ((i + 1) % 15 == 0 && (i + 1) < words.length) {
                        summary.append("\n");
                    }
                }
                summary.append("\n");
            }
        }
        return summary.toString().trim();
    }

    private Map<String, Integer> getWordCounts(String text) {
        Map<String, Integer> wordCounts = new HashMap<>();
        text = text.trim();
        String[] words = text.split("\\s+");
        for (String w : words) {
            wordCounts.put(w, wordCounts.getOrDefault(w, 0) +
                    (wordCounts.containsKey(w) ? 2 : 1));
        }
        return wordCounts;
    }

    private Map<String, Integer> filterStopWords(Map<String, Integer> words) {
        words.keySet().removeIf(STOP_WORDS::contains);
        return words;
    }

    private List<String> sortByFreqThenDropFreq(Map<String, Integer> wordFrequencies) {
        List<String> sorted = new ArrayList<>(wordFrequencies.keySet());
        sorted.sort((a, b) -> wordFrequencies.get(b) - wordFrequencies.get(a));
        return sorted;
    }

    private String[] getSentences(String text) {
        text = text.replaceAll("(Mr|Ms|Dr|Jan|Feb|Mar|Apr|Jun|Jul|Aug|Sep|Sept|Oct|Nov|Dec|St|Prof|Mrs|Gen|Corp|Sr|Jr|cm|Ltd|Col|vs|Capt|Univ|Sgt|ft|in|Ave|Lt|etc|mm)\\.", "$1");
        text = text.replace("\n", " ").replace("\r", " ").replaceAll("\\s+", " ").trim();
        return text.split("(?<=[.!?])\\s+");
    }

    private String search(String[] sentences, String word) {
        for (String sentence : sentences) {
            if (sentence.contains(word)) {
                return sentence;
            }
        }
        return null;
    }
}
