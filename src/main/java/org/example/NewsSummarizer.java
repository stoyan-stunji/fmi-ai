package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.stream.*;
import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.commons.math3.linear.*;

public class NewsSummarizer {
    private JFrame frame;
    private JTextField kField;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new NewsSummarizer().initialize();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void initialize() {
        frame = new JFrame("News Summarizer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 200);
        frame.setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel label = new JLabel("Enter number of clusters (k):");
        kField = new JTextField("10");  // Default value
        JButton generateButton = new JButton("Generate Summaries");

        generateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int k = Integer.parseInt(kField.getText());
                    if (k <= 0) {
                        JOptionPane.showMessageDialog(frame, "Please enter a positive number for k");
                        return;
                    }
                    frame.dispose();  // Close the input window
                    run(k);          // Run the analysis with user's k value
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Please enter a valid number for k");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
                }
            }
        });

        inputPanel.add(label);
        inputPanel.add(kField);
        inputPanel.add(generateButton);

        frame.add(inputPanel, BorderLayout.CENTER);
        frame.setLocationRelativeTo(null);  // Center on screen
        frame.setVisible(true);
    }

    public void run(int k) throws Exception {
        List<String> documents = loadDocuments("D:/Dji/3_education/university_FMI_1-4_course/semester_6/fmi_ai/src/main/data-prep/20newsgroups");
        System.out.println("Loaded " + documents.size() + " documents");
        if (documents.isEmpty()) {
            System.err.println("No documents loaded! Check the data path.");
            return;
        }

        documents = documents.stream()
                .map(NewsSummarizer::cleanText)
                .filter(d -> !isMostlyGarbage(d))
                .filter(d -> d.length() > 100)  // minimum reasonable length
                .collect(Collectors.toList());

        List<List<String>> tokensList = documents.stream()
                .map(NewsSummarizer::cleanText)  // Clean before tokenizing
                .map(NewsSummarizer::tokenize)
                .collect(Collectors.toList());

        try {
            RealMatrix tfidfMatrix = computeTFIDF(tokensList);
            int[] labels = KMeansClusterer.cluster(tfidfMatrix, k);  // Use user-provided k

            Summarizer summarizer = new Summarizer();

            // Create a progress dialog
            JProgressBar progressBar = new JProgressBar(0, k);
            JDialog progressDialog = new JDialog();
            progressDialog.setTitle("Generating Summaries");
            progressDialog.setLayout(new BorderLayout());
            progressDialog.add(new JLabel("Generating cluster summaries..."), BorderLayout.NORTH);
            progressDialog.add(progressBar, BorderLayout.CENTER);
            progressDialog.setSize(300, 100);
            progressDialog.setLocationRelativeTo(null);
            progressDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            progressDialog.setVisible(true);

            for (int i = 0; i < k; i++) {
                System.out.println("=== Cluster " + i + " ===");
                summarizeCluster(documents, labels, i, summarizer);
                progressBar.setValue(i + 1);
            }

            progressDialog.dispose();
            JOptionPane.showMessageDialog(null, "Successfully generated summaries for " + k + " clusters!");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    static boolean isMostlyGarbage(String text) {
        if (text.length() < 50) return true;  // very short texts are suspicious

        // Count letters only (not digits)
        long alphaCount = text.chars().filter(ch -> Character.isLetter(ch)).count();
        double ratio = (double) alphaCount / text.length();

        // Also check for excessive uppercase
        long upperCount = text.chars().filter(ch -> Character.isUpperCase(ch)).count();
        double upperRatio = (double) upperCount / text.length();

        return ratio < 0.3 || upperRatio > 0.7;
    }


    static List<String> loadDocuments(String path) throws Exception {
        try (Stream<Path> paths = Files.walk(Paths.get(path))) {
            return paths
                    .filter(Files::isRegularFile)
                    .map(p -> {
                        try {
                            String content = Files.readString(p);
                            // Filter out likely binary or corrupted files
                            if (content.chars().filter(ch -> Character.isISOControl(ch) && ch != '\n' && ch != '\r').count() > 100) {
                                return "";
                            }
                            return content;
                        } catch (Exception e) {
                            return "";
                        }
                    })
                    .filter(s -> !s.isBlank())
                    .collect(Collectors.toList());
        }
    }

    static List<String> tokenize(String text) {
        List<String> result = new ArrayList<>();
        try (Analyzer analyzer = new StandardAnalyzer()) {
            TokenStream ts = analyzer.tokenStream(null, text);
            ts.reset();
            while (ts.incrementToken()) {
                result.add(ts.getAttribute(CharTermAttribute.class).toString());
            }
            ts.end(); ts.close();
        } catch (Exception e) { e.printStackTrace(); }
        return result;
    }

    static RealMatrix computeTFIDF(List<List<String>> documents) {
        int MAX_VOCAB_SIZE = 10000;

// Step 1: Count word frequencies
        Map<String, Integer> wordCounts = new HashMap<>();
        for (List<String> doc : documents) {
            for (String word : doc) {
                wordCounts.put(word, wordCounts.getOrDefault(word, 0) + 1);
            }
        }

// Step 2: Get top-N most frequent words
        List<String> topWords = wordCounts.entrySet().stream()
                .sorted((a, b) -> b.getValue() - a.getValue())
                .limit(MAX_VOCAB_SIZE)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

// Step 3: Build vocab with limited size
        Map<String, Integer> vocab = new HashMap<>();
        for (int i = 0; i < topWords.size(); i++) {
            vocab.put(topWords.get(i), i);
        }


        int[][] tf = new int[documents.size()][vocab.size()];
        for (int i = 0; i < documents.size(); i++) {
            for (String word : documents.get(i)) {
                Integer index = vocab.get(word);
                if (index != null) {
                    tf[i][index]++;
                } /*else {
                    System.err.println("Warning: Word not found in vocab: " + word);
                }*/
            }
        }


        double[][] tfidf = new double[documents.size()][vocab.size()];
        for (int j = 0; j < vocab.size(); j++) {
            int df = 0;
            for (int i = 0; i < documents.size(); i++) {
                if (tf[i][j] > 0) df++;
            }
            double idf = Math.log((1.0 + documents.size()) / (1 + df));
            for (int i = 0; i < documents.size(); i++) {
                tfidf[i][j] = tf[i][j] * idf;
            }
        }

        return MatrixUtils.createRealMatrix(tfidf);
    }

    static String cleanText(String text) {
        // Remove all non-printable characters except newline and tab
        text = text.replaceAll("[^\\p{Print}\\n\\t]", " ");

        // Remove long sequences of repeating characters (----, ____, ====, ~~~~ etc.)
        text = text.replaceAll("-{500,}|_{500,}|={500,}|\\*{500,}|~{500,}", " ");

        // Remove garbage patterns like "begin big1.zip" or "M4$L ,!V?"
        text = text.replaceAll("\\b(begin|start|file|name)\\s+[^\\s]{3,}\\.(zip|exe|rar)\\b", " ");
        text = text.replaceAll("\\b[A-Z0-9]+[^a-zA-Z0-9\\s]+[A-Z0-9]*\\b", " ");

        // Remove garbage lines starting with • followed by nonsense
        text = text.replaceAll("(?m)^•\\s*[^a-zA-Z0-9\\s]{2,}.*$", " ");

        // Remove M-prefixed garbage (common in encoded text)
        text = text.replaceAll("\\bM[A-Z0-9][^a-zA-Z0-9\\s]{1,}", " ");

        // Remove encoded-looking patterns
        text = text.replaceAll("\\b[A-Z0-9]{2,}[^a-zA-Z0-9\\s]+[A-Z0-9]{2,}\\b", " ");

        // Remove angle bracket patterns
        text = text.replaceAll("\\s*>[^a-zA-Z0-9\\s]+\\b", " ");
        text = text.replaceAll("\\b[^a-zA-Z0-9\\s]+<\\s*", " ");

        // Remove specific garbage sequences from your example
        text = text.replaceAll("':76L\\$!", " ");
        text = text.replaceAll("\\[\\s*P\\s*0E[^a-zA-Z]+", " ");
        text = text.replaceAll("F/\\s*>\\^\\..+@P\\?&N\\[", " ");

        // Remove content in brackets with mostly garbage
        text = text.replaceAll("\\[[^\\]]*[^a-zA-Z0-9\\s]{3,}[^\\]]*\\]", " ");
        text = text.replaceAll("\\([^)]*[^a-zA-Z\\s]{3,}[^)]*\\)", " ");

        // Remove empty parentheses or brackets
        text = text.replaceAll("[\\[({]\\s*[])}]", " ");

        // Remove document part markers
        text = text.replaceAll("-+\\s*Part\\s+\\d+\\s+of\\s+\\d+\\s*-+", " ");

        // --- NEW: Remove all bracketed words entirely ---
        text = text.replaceAll("\\[[^\\]]*\\]", " ");  // remove everything in [ ]
        text = text.replaceAll("\\{[^}]*\\}", " ");    // remove everything in { }
        text = text.replaceAll("//[^\\s]+", " ");      // remove everything starting with // until next space

        // Remove remaining isolated special characters
        text = text.replaceAll("(^|\\s)[^\\w\\s]+($|\\s)", " ");

        // Collapse multiple spaces and trim
        text = text.replaceAll("\\s+", " ").trim();

        // Filter lines with mostly garbage characters
        text = Arrays.stream(text.split("\\n"))
                .filter(line -> {
                    int alpha = line.replaceAll("[^a-zA-Z]", "").length();
                    return alpha * 2 > line.length();
                })
                .collect(Collectors.joining("\n"));

        return text;
    }



    static void summarizeCluster(List<String> docs, int[] labels, int clusterId, Summarizer summarizer) {
        List<String> clusterDocs = IntStream.range(0, labels.length)
                .filter(i -> labels[i] == clusterId)
                .mapToObj(docs::get)
                .collect(Collectors.toList());

        if (clusterDocs.isEmpty()) {
            System.out.println("No documents found in cluster " + clusterId);
            return;
        }

        int maxSummarySentences = 15;
        for (int i = 0; i < Math.min(3, clusterDocs.size()); i++) {
            String doc = clusterDocs.get(i);
            String summary = summarizer.Summarize(doc, maxSummarySentences);
            saveSummaryToFile(summary, clusterId, i);
        }
    }

    static void saveSummaryToFile(String summary, int clusterId, int docIndex) {
        try {
            Path outputDir = Paths.get("summaries");
            if (!Files.exists(outputDir)) {
                Files.createDirectories(outputDir);
            }
            String fileName = "cluster_" + clusterId + "_doc_" + docIndex + ".txt";
            Path filePath = outputDir.resolve(fileName);
            Files.writeString(filePath, summary);
            System.out.println("Saved summary to " + filePath.toAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

// Summarizer class (slightly cleaned, no package or android imports)
class Summarizer {
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "a", "able", "about", "above", "after", "again", "against", "ain", "all", "also", "am", "an", "and", "any",
            "aren", "are", "aren't", "as", "at", "be", "because", "been", "before", "being", "below", "between", "both",
            "but", "by", "can", "cannot", "could", "couldn", "couldn't", "did", "didn", "didn't", "do", "does", "doesn",
            "doesn't", "doing", "don", "don't", "down", "during", "each", "either", "else", "ever", "every", "few", "for",
            "from", "further", "get", "got", "had", "hadn", "hadn't", "has", "hasn", "hasn't", "have", "haven", "haven't",
            "having", "he", "her", "here", "hers", "herself", "him", "himself", "his", "how", "however", "i", "if", "in",
            "into", "is", "isn", "isn't", "it", "it's", "its", "itself", "just", "let", "like", "likely", "may", "might",
            "mightn", "mightn't", "mine", "more", "most", "must", "mustn", "mustn't", "my", "myself", "neither", "no",
            "nor", "not", "now", "of", "off", "often", "on", "once", "only", "or", "other", "ought", "our", "ours",
            "ourselves", "out", "over", "own", "same", "s", "said", "say", "says", "she", "she's", "should", "should've",
            "shouldn", "shouldn't", "so", "some", "such", "t", "than", "that", "that'll", "the", "their", "theirs",
            "them", "themselves", "then", "there", "these", "they", "they'd", "they'll", "they're", "they've", "this",
            "those", "through", "to", "too", "under", "until", "up", "us", "very", "was", "wasn", "wasn't", "we", "were",
            "weren", "weren't", "what", "when", "where", "which", "while", "who", "whom", "why", "will", "with", "won",
            "won't", "would", "wouldn", "wouldn't", "y", "you", "you'd", "you'll", "you're", "you've", "your", "yours",
            "yourself", "yourselves"
    ));


    private Map<String, Integer> getWordCounts(String text) {
        Map<String,Integer> allWords = new HashMap<>();
        text = text.trim();
        String[] words = text.split("\\s+");

        for (String w : words) {
            if (allWords.containsKey(w)) {
                allWords.put(w, allWords.get(w) + 2);
            } else {
                allWords.put(w, 1);
            }
        }
        return allWords;
    }

    private Map<String,Integer> filterStopWords(Map<String, Integer> d) {
        d.keySet().removeIf(STOP_WORDS::contains);
        return d;
    }

    private List<String> sortByFreqThenDropFreq(Map<String,Integer> wordFrequencies) {
        List<String> sortedCollection = new ArrayList<>(wordFrequencies.keySet());
        sortedCollection.sort((a, b) -> wordFrequencies.get(b) - wordFrequencies.get(a));
        return sortedCollection;
    }

    private String[] getSentences(String text) {
        text = text.replaceAll("(Mr|Ms|Dr|Jan|Feb|Mar|Apr|Jun|Jul|Aug|Sep|Sept|Oct|Nov|Dec|St|Prof|Mrs|Gen|Corp|Sr|Jr|cm|Ltd|Col|vs|Capt|Univ|Sgt|ft|in|Ave|Lt|etc|mm)\\.", "$1");
        text = text.replace("\n", " ").replace("\r", " ").replaceAll("\\s+", " ").trim();

        // split sentences by punctuation
        return text.split("(?<=[.!?])\\s+");
    }

    private String search(String[] sentences, String word) {
        for (String sentence : sentences) {
            if (sentence.contains(word)) return sentence;
        }
        return null;
    }

    public String Summarize(String text, int maxSummarySize) {
        if (text == null || text.trim().isEmpty()) {
            return "Nothing to summarize...";
        }

        Map<String, Integer> wordFrequencies = filterStopWords(getWordCounts(text));
        List<String> sorted = sortByFreqThenDropFreq(wordFrequencies);
        String[] sentences = getSentences(text);

        if (sentences.length == 0) return "";

        List<String> setSummarySentences = new ArrayList<>();
        setSummarySentences.add(sentences[0]); // always include the first sentence

        for (String word : sorted) {
            String matchingSentence = search(sentences, word);
            if (matchingSentence != null && !setSummarySentences.contains(matchingSentence)) {
                setSummarySentences.add(matchingSentence);
            }
            if (setSummarySentences.size() >= maxSummarySize) break;
        }

        StringBuilder summary = new StringBuilder();
        for (String sentence : sentences) {
            if (setSummarySentences.contains(sentence)) {
                String[] words = sentence.trim().split("\\s+");
                summary.append("\t"); // indent the paragraph
                for (int i = 0; i < words.length; i++) {
                    summary.append(words[i]).append(" ");
                    if ((i + 1) % 15 == 0 && (i + 1) < words.length) {
                        summary.append("\n"); // new line and indent again
                    }
                }
                summary.append("\n"); // extra space between paragraphs
            }
        }

        return summary.toString().trim();
    }

}