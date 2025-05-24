package org.example.service.utility;

import org.example.datastructures.AbstractSummarizer;
import org.example.storage.NewsRepository;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ClusterSummarizer {
    private final AbstractSummarizer summarizer;
    private final NewsRepository repository;

    private final static int maxSummarySentences = 20;
    private final static int minClustersByDocs = 1;

    public ClusterSummarizer(AbstractSummarizer summarizer, NewsRepository repository) {
        this.summarizer = summarizer;
        this.repository = repository;
    }

    public void summarizeCluster(List<String> docs, int[] labels, int clusterId) throws Exception {
        List<String> clusterDocs = IntStream.range(0, labels.length)
                .filter(i -> labels[i] == clusterId)
                .mapToObj(docs::get)
                .collect(Collectors.toList());
        if (clusterDocs.isEmpty()) {
            return;
        }
        for (int i = 0; i < Math.min(minClustersByDocs, clusterDocs.size()); i++) {
            String summary = summarizer.summarize(clusterDocs.get(i), maxSummarySentences);
            repository.saveSummary(summary, clusterId, i);
        }
    }
}
