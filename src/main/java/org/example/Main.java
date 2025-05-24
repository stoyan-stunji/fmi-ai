package org.example;

public class Main {
    public static void main(String[] args) throws Exception {
        try {
            NewsSummarizer newsSummarizer = new NewsSummarizer();
            newsSummarizer.run(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}