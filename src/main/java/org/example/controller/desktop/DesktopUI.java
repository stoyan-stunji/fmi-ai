package org.example.controller.desktop;

import org.example.controller.ProgressCallback;
import org.example.service.DocumentSummarizerServiceImpl;

import javax.swing.*;
import java.awt.*;

public class DesktopUI {
    private final String title;
    private final DocumentSummarizerServiceImpl service;

    public DesktopUI(String title, DocumentSummarizerServiceImpl service) {
        this.title = title;
        this.service = service;
    }

    public void initialize() {
        JFrame frame = createMainFrame();
        JPanel inputPanel = buildInputPanel(frame);
        frame.add(inputPanel, BorderLayout.CENTER);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JFrame createMainFrame() {
        JFrame frame = new JFrame(this.title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 200);
        frame.setLayout(new BorderLayout());
        return frame;
    }

    private JPanel buildInputPanel(JFrame frame) {
        JPanel inputPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel label = new JLabel("Enter number of clusters (k):");
        JTextField kField = new JTextField("");
        JButton generateButton = new JButton("Generate Summaries");

        generateButton.addActionListener(e -> onGenerateClicked(kField, frame));

        inputPanel.add(label);
        inputPanel.add(kField);
        inputPanel.add(generateButton);

        return inputPanel;
    }

    private void onGenerateClicked(JTextField kField, JFrame frame) {
        try {
            int k = Integer.parseInt(kField.getText());
            if (k <= 0) {
                showMessage(frame, "Please enter a positive number for k!");
                return;
            }
            frame.dispose();
            showProgressDialog(k);
        } catch (NumberFormatException ex) {
            showMessage(frame, "Please enter a valid number for k!");
        }
    }

    private void showMessage(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message);
    }

    private void showProgressDialog(int k) {
        JProgressBar progressBar = new JProgressBar(0, k);
        JDialog progressDialog = createProgressDialog(progressBar);

        new Thread(() -> {
            try {
                service.generateSummaries(k, createProgressCallback(progressBar, progressDialog, k));
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    progressDialog.dispose();
                    showErrorDialog("Error: " + e.getMessage());
                });
            }
        }).start();
    }

    private JDialog createProgressDialog(JProgressBar progressBar) {
        JDialog progressDialog = new JDialog();
        progressDialog.setTitle("Generating Summaries");
        progressDialog.setLayout(new BorderLayout());
        progressDialog.add(new JLabel("Generating cluster summaries..."), BorderLayout.NORTH);
        progressDialog.add(progressBar, BorderLayout.CENTER);
        progressDialog.setSize(300, 100);
        progressDialog.setLocationRelativeTo(null);
        progressDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        progressDialog.setVisible(true);
        return progressDialog;
    }

    private ProgressCallback createProgressCallback(JProgressBar progressBar, JDialog dialog,
                                                    int total) {
        return new ProgressCallback() {
            @Override
            public void onProgress(int current, int total) {
                SwingUtilities.invokeLater(() -> progressBar.setValue(current));
            }

            @Override
            public void onComplete() {
                SwingUtilities.invokeLater(() -> {
                    dialog.dispose();
                    JOptionPane.showMessageDialog(null,
                            "Successfully generated summaries for "
                                    + total + " clusters!");
                });
            }

            @Override
            public void onError(Exception e) {
                SwingUtilities.invokeLater(() -> {
                    dialog.dispose();
                    showErrorDialog("Error: " + e.getMessage());
                });
            }
        };
    }

    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(null, message,
                "Error", JOptionPane.ERROR_MESSAGE);
    }
}
