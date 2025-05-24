package org.example.service.utility;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TFIDFVectorizer {
    private static final int MAX_VOCAB_SIZE = 1000;

    public RealMatrix computeTFIDF(List<List<String>> documents) {
        Map<String, Integer> wordCounts = countWordFrequencies(documents);
        List<String> topWords = selectTopVocabulary(wordCounts);
        Map<String, Integer> vocab = buildVocabIndexMap(topWords);
        int[][] tf = computeTermFrequencyMatrix(documents, vocab);
        return computeTFIDFMatrix(documents.size(), vocab.size(), tf);
    }

    private Map<String, Integer> countWordFrequencies(List<List<String>> documents) {
        Map<String, Integer> wordCounts = new HashMap<>();
        for (List<String> doc : documents) {
            for (String word : doc) {
                wordCounts.put(word, wordCounts.getOrDefault(word, 0) + 1);
            }
        }
        return wordCounts;
    }

    private List<String> selectTopVocabulary(Map<String, Integer> wordCounts) {
        return wordCounts.entrySet().stream()
                .sorted((a, b) -> b.getValue() - a.getValue())
                .limit(MAX_VOCAB_SIZE)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private Map<String, Integer> buildVocabIndexMap(List<String> topWords) {
        return IntStream.range(0, topWords.size())
                .boxed()
                .collect(Collectors.toMap(topWords::get, i -> i));
    }

    private int[][] computeTermFrequencyMatrix(List<List<String>> documents, Map<String, Integer> vocab) {
        int[][] tf = new int[documents.size()][vocab.size()];
        for (int i = 0; i < documents.size(); i++) {
            for (String word : documents.get(i)) {
                Integer index = vocab.get(word);
                if (index != null) {
                    tf[i][index]++;
                }
            }
        }
        return tf;
    }

    private RealMatrix computeTFIDFMatrix(int docCount, int vocabSize, int[][] tf) {
        double[][] tfidf = new double[docCount][vocabSize];
        for (int j = 0; j < vocabSize; j++) {
            int df = 0;
            for (int i = 0; i < docCount; i++) {
                if (tf[i][j] > 0) {
                    df++;
                }
            }
            double idf = Math.log((1.0 + docCount) / (1 + df));
            for (int i = 0; i < docCount; i++) {
                tfidf[i][j] = tf[i][j] * idf;
            }
        }
        return MatrixUtils.createRealMatrix(tfidf);
    }
}
