package org.example.datastructures.kmeans;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import java.util.Arrays;
import java.util.Random;

public class KMeansClusterer {
    private static final int MAX_ITERATIONS = 100;
    private static final CentroidInitializer initializer = new CentroidInitializer();
    private static final KMeansClusterAssigner assigner = new KMeansClusterAssigner();
    private static final CentroidUpdater updater = new CentroidUpdater();

    public KMeansClusterer() {
    }

    public static int[] cluster(RealMatrix data, int k) {
        int[] labels = new int[data.getRowDimension()];
        RealVector[] centroids;

        Random rand = new Random(42);
        centroids = initializer.initialize(data, k, rand);

        boolean changed = true;
        int iteration = 0;

        while (changed && iteration < MAX_ITERATIONS) {
            iteration++;
            int[] newLabels = assigner.assignClusters(data, centroids);
            changed = !Arrays.equals(labels, newLabels);
            labels = newLabels;
            centroids = updater.updateCentroids(data, labels, k, rand);
        }

        return labels;
    }
}
