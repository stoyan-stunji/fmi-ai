package org.example.controller;

public interface ProgressCallback {
    void onProgress(int current, int total);
    void onComplete();
    void onError(Exception e);
}
