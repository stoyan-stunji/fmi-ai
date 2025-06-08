package org.example.storage.implementation.filesystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DocumentReaderWriter {
    private final String basePath;

    public DocumentReaderWriter(String basePath) {
        this.basePath = basePath;
    }

    public List<String> readAllDocuments() throws IOException {
        try (Stream<Path> paths = Files.walk(Paths.get(basePath))) {
            return paths
                    .filter(Files::isRegularFile)
                    .map(this::readDocumentSafely)
                    .filter(content -> !content.isBlank())
                    .collect(Collectors.toList());
        }
    }

    public void writeDocument(String content, String relativePath) throws IOException {
        Path fullPath = Paths.get(basePath, relativePath);
        createParentDirectories(fullPath);
        Files.writeString(fullPath, content);
    }

    private String readDocumentSafely(Path filePath) {
        try {
            String content = Files.readString(filePath);
            if (hasTooManyControlCharacters(content)) {
                return "";
            }
            return content;
        } catch (Exception e) {
            return "";
        }
    }

    private boolean hasTooManyControlCharacters(String content) {
        return content.chars()
                .filter(ch -> Character.isISOControl(ch) && ch != '\n' && ch != '\r')
                .count() > 100;
    }

    private void createParentDirectories(Path filePath) throws IOException {
        Path parent = filePath.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }
    }
}