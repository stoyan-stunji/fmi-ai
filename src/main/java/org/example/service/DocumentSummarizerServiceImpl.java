package org.example.service;

import org.apache.commons.math3.linear.RealMatrix;
import org.example.datastructures.AbstractSummarizer;
import org.example.datastructures.kmeans.KMeansClusterer;
import org.example.service.utility.ClusterSummarizer;
import org.example.service.utility.DocumentTokenizer;
import org.example.service.utility.DocumentPreprocessor;
import org.example.service.utility.TFIDFVectorizer;
import org.example.storage.DocumentRepository;
import org.example.controller.ProgressCallback;

import java.util.List;

public class DocumentSummarizerServiceImpl implements DocumentSummarizerService {
    private final DocumentRepository newsRepository;
    private final ClusterSummarizer clusterSummarizer;
    private final DocumentPreprocessor preprocessor;
    private final DocumentTokenizer tokenizer;
    private final TFIDFVectorizer vectorizer;

    public DocumentSummarizerServiceImpl(DocumentRepository newsRepository, AbstractSummarizer summarizer) {
        this.newsRepository = newsRepository;
        this.clusterSummarizer = new ClusterSummarizer(summarizer, newsRepository);
        this.preprocessor = new DocumentPreprocessor();
        this.tokenizer = new DocumentTokenizer();
        this.vectorizer = new TFIDFVectorizer();
    }

    @Override
    public void generateSummaries(int k, ProgressCallback callback) {
        try {
            List<String> rawDocs = loadDocuments();
            List<String> cleanedDocs = preprocessDocuments(rawDocs);
            List<List<String>> tokens = tokenizeDocuments(cleanedDocs);
            RealMatrix tfidfMatrix = vectorizeDocuments(tokens);
            int[] labels = clusterDocuments(tfidfMatrix, k);
            summarizeClusters(k, cleanedDocs, labels, callback);
            notifyCompletion(callback);
        } catch (Exception e) {
            handleError(e, callback);
            throw new RuntimeException("NewsSummarizerServiceImpl::" +
                    "generateSummaries::error_generating_summaries", e);
        }
    }

    private List<String> loadDocuments() throws Exception {
        List<String> docs = newsRepository.loadNews();
        if (docs.isEmpty()) {
            throw new RuntimeException("NewsSummarizerServiceImpl::" +
                    "loadDocuments::NO_documents_loaded");
        }
        return docs;
    }

    private List<String> preprocessDocuments(List<String> rawDocs) {
        return preprocessor.preprocess(rawDocs);
    }

    private List<List<String>> tokenizeDocuments(List<String> cleanedDocs) {
        return tokenizer.tokenizeDocuments(cleanedDocs);
    }

    private RealMatrix vectorizeDocuments(List<List<String>> tokens) {
        return vectorizer.computeTFIDF(tokens);
    }

    private int[] clusterDocuments(RealMatrix tfidfMatrix, int k) {
        return KMeansClusterer.cluster(tfidfMatrix, k);
    }

    private void summarizeClusters(int k, List<String> docs, int[] labels,
                                   ProgressCallback callback) throws Exception {
        for (int i = 0; i < k; i++) {
            clusterSummarizer.summarizeCluster(docs, labels, i);
            if (callback != null) callback.onProgress(i + 1, k);
        }
    }

    private void notifyCompletion(ProgressCallback callback) {
        if (callback != null) {
            callback.onComplete();
        }
    }

    private void handleError(Exception e, ProgressCallback callback) {
        if (callback != null) {
            callback.onError(e);
        }
    }
}
