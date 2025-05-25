package org.example.service;

import org.example.controller.ProgressCallback;

public interface DocumentSummarizerService {
    void generateSummaries(int k, ProgressCallback callback);
}
