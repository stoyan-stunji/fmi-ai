package org.example.storage;

import java.util.*;

public interface DocumentRepository {
    List<String> loadDocuments() throws Exception;
    void saveSummary(String summary, int clusterId, int docIndex) throws Exception;
}