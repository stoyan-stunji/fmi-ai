package org.example.service.utility;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DocumentTokenizer {
    public List<List<String>> tokenizeDocuments(List<String> documents) {
        return documents.stream()
                .map(this::tokenize)
                .collect(Collectors.toList());
    }

    public List<String> tokenize(String text) {
        List<String> result = new ArrayList<>();
        try (Analyzer analyzer = new StandardAnalyzer()) {
            TokenStream ts = analyzer.tokenStream(null, text);
            ts.reset();
            while (ts.incrementToken()) {
                result.add(ts.getAttribute(CharTermAttribute.class).toString());
            }
            ts.end(); ts.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}

