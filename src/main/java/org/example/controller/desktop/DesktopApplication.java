package org.example.controller.desktop;

import org.example.storage.implementation.NewsRepositoryFactory;
import org.example.controller.PythonScriptRunner;
import org.example.datastructures.summarizers.TwentyNewsGroupsSummarizer;
import org.example.service.NewsSummarizerServiceImpl;
import org.example.storage.implementation.NewsRepositoryImpl;

import java.nio.file.Path;
import java.nio.file.Paths;

public class DesktopApplication {
    private static final String PYTHON_EXEC = "python";
    private static final Path SCRIPT_PATH = Paths.get("src", "main", "data-prep", "export-twenty-news-groups.py");
    private static final Path DATA_DIR = Paths.get("src", "main", "data-prep", "20newsgroups");
    private static final String SUMMARIES_DIR = "summaries";

    public void run() {
        try {
            PythonScriptRunner pythonRunner = new PythonScriptRunner(PYTHON_EXEC, SCRIPT_PATH.toString());
            pythonRunner.run();

            NewsRepositoryFactory repositoryFactory = new NewsRepositoryFactory(DATA_DIR.toString(), SUMMARIES_DIR);
            NewsRepositoryImpl repository = repositoryFactory.create();

            TwentyNewsGroupsSummarizer summarizer = new TwentyNewsGroupsSummarizer();
            NewsSummarizerServiceImpl service = new NewsSummarizerServiceImpl(repository, summarizer);

            DesktopUI ui = new DesktopUI("20 NEWS GROUP SUMMARIZER", service);
            ui.initialize();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
