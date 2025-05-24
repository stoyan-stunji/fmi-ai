package org.example.service;

import org.example.controller.ProgressCallback;

public interface NewsSummarizerService {
    void generateSummaries(int k, ProgressCallback callback);
}
