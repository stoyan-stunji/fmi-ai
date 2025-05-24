package org.example;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.ArrayRealVector;

import java.util.Random;
import java.util.Arrays;

public class KMeansClusterer {

    /**
     * Cluster rows of the given matrix into k clusters.
     * @param data RealMatrix with rows as points to cluster
     * @param k Number of clusters
     * @return int[] cluster labels for each row
     */
    public static int[] cluster(RealMatrix data, int k) {
        int nSamples = data.getRowDimension();
        int nFeatures = data.getColumnDimension();
        int[] labels = new int[nSamples];
        RealVector[] centroids = new RealVector[k];

        Random rand = new Random(42);

        // Initialize centroids randomly from data points
        for (int i = 0; i < k; i++) {
            int randomIndex = rand.nextInt(nSamples);
            centroids[i] = new ArrayRealVector(data.getRow(randomIndex));
        }

        boolean changed = true;
        int maxIterations = 100;
        int iteration = 0;

        while (changed && iteration < maxIterations) {
            changed = false;
            iteration++;

            // Assign points to nearest centroid
            for (int i = 0; i < nSamples; i++) {
                RealVector point = new ArrayRealVector(data.getRow(i));
                int bestCluster = 0;
                double bestDist = point.getDistance(centroids[0]);

                for (int c = 1; c < k; c++) {
                    double dist = point.getDistance(centroids[c]);
                    if (dist < bestDist) {
                        bestDist = dist;
                        bestCluster = c;
                    }
                }

                if (labels[i] != bestCluster) {
                    labels[i] = bestCluster;
                    changed = true;
                }
            }

            // Update centroids
            RealVector[] newCentroids = new RealVector[k];
            int[] counts = new int[k];
            for (int i = 0; i < k; i++) {
                newCentroids[i] = new ArrayRealVector(nFeatures);
            }

            for (int i = 0; i < nSamples; i++) {
                newCentroids[labels[i]] = newCentroids[labels[i]].add(new ArrayRealVector(data.getRow(i)));
                counts[labels[i]]++;
            }

            for (int i = 0; i < k; i++) {
                if (counts[i] > 0) {
                    newCentroids[i] = newCentroids[i].mapDivide(counts[i]);
                } else {
                    // Reinitialize centroid if no points assigned
                    int randomIndex = rand.nextInt(nSamples);
                    newCentroids[i] = new ArrayRealVector(data.getRow(randomIndex));
                }
            }

            centroids = newCentroids;
        }

        return labels;
    }
}

