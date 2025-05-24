package org.example.storage.implementation;

import org.example.storage.NewsRepository;
import org.example.storage.implementation.filesystem.NewsReaderWriter;

import java.io.IOException;
import java.util.List;

public class NewsRepositoryImpl implements NewsRepository {
    private final NewsReaderWriter newsReader;
    private final NewsReaderWriter summaryWriter;

    public NewsRepositoryImpl(String newsPath, String summariesPath) {
        this.newsReader = new NewsReaderWriter(newsPath);
        this.summaryWriter = new NewsReaderWriter(summariesPath);
    }

    @Override
    public List<String> loadNews() throws IOException {
        return newsReader.readAllDocuments();
    }

    @Override
    public void saveSummary(String summary, int clusterId, int docIndex) throws IOException {
        String fileName = "cluster_" + clusterId + "_doc_" + docIndex + ".txt";
        summaryWriter.writeDocument(summary, fileName);
    }
}
