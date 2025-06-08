package org.example.controller;

import org.example.storage.implementation.DocumentRepositoryImpl;

public class DocumentRepositoryFactory {
    private final String dataDir;
    private final String summariesDir;

    public DocumentRepositoryFactory(String dataDir, String summariesDir) {
        this.dataDir = dataDir;
        this.summariesDir = summariesDir;
    }

    public DocumentRepositoryImpl create() {
        return new DocumentRepositoryImpl(dataDir, summariesDir);
    }
}
