package org.example.controller.desktop;

import org.example.controller.DocumentRepositoryFactory;
import org.example.controller.PythonScriptRunner;
import org.example.datastructures.summarizers.TwentyNewsGroupsSummarizer;
import org.example.service.DocumentSummarizerServiceImpl;
import org.example.storage.implementation.DocumentRepositoryImpl;

import java.nio.file.Path;
import java.nio.file.Paths;

public class DesktopApplication {
    private static final String PYTHON_EXEC = "python";
    private static final Path SCRIPT_PATH = Paths.get("src", "main", "python", "export-twenty-news-groups.py");
    private static final Path DATA_DIR = Paths.get("src", "main", "python", "20newsgroups");
    private static final String SUMMARIES_DIR = "summaries";

    public void run() {
        try {
            PythonScriptRunner pythonRunner = new PythonScriptRunner(PYTHON_EXEC, SCRIPT_PATH.toString());
            pythonRunner.run();

            DocumentRepositoryFactory repositoryFactory = new DocumentRepositoryFactory(DATA_DIR.toString(), SUMMARIES_DIR);
            DocumentRepositoryImpl repository = repositoryFactory.create();

            TwentyNewsGroupsSummarizer summarizer = new TwentyNewsGroupsSummarizer();
            DocumentSummarizerServiceImpl service = new DocumentSummarizerServiceImpl(repository, summarizer);

            DesktopUI ui = new DesktopUI("20 NEWS GROUP SUMMARIZER", service);
            ui.initialize();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
