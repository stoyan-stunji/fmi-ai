package org.example.storage;

import java.util.*;

public interface NewsRepository {
    List<String> loadNews() throws Exception;
    void saveSummary(String summary, int clusterId, int docIndex) throws Exception;
}