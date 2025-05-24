package org.example.storage.implementation;

public class NewsRepositoryFactory {
    private final String dataDir;
    private final String summariesDir;

    public NewsRepositoryFactory(String dataDir, String summariesDir) {
        this.dataDir = dataDir;
        this.summariesDir = summariesDir;
    }

    public NewsRepositoryImpl create() {
        return new NewsRepositoryImpl(dataDir, summariesDir);
    }
}
