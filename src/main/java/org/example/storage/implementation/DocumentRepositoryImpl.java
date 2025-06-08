package org.example.storage.implementation;

import org.example.storage.DocumentRepository;
import org.example.storage.implementation.filesystem.DocumentReaderWriter;

import java.io.IOException;
import java.util.List;

public class DocumentRepositoryImpl implements DocumentRepository {
    private final DocumentReaderWriter documentsReader;
    private final DocumentReaderWriter summaryWriter;

    public DocumentRepositoryImpl(String docsPath, String summariesPath) {
        this.documentsReader = new DocumentReaderWriter(docsPath);
        this.summaryWriter = new DocumentReaderWriter(summariesPath);
    }

    @Override
    public List<String> loadDocuments() throws IOException {
        return documentsReader.readAllDocuments();
    }

    @Override
    public void saveSummary(String summary, int clusterId, int docIndex) throws IOException {
        String fileName = "cluster_" + clusterId + "_doc_" + docIndex + ".txt";
        summaryWriter.writeDocument(summary, fileName);
    }
}
