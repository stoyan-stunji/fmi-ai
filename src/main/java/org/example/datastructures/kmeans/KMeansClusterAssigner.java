package org.example.datastructures.kmeans;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.ArrayRealVector;

public class KMeansClusterAssigner {
    public int[] assignClusters(RealMatrix data, RealVector[] centroids) {
        int nSamples = data.getRowDimension();
        int[] labels = new int[nSamples];

        for (int i = 0; i < nSamples; i++) {
            RealVector point = new ArrayRealVector(data.getRow(i));
            int bestCluster = 0;
            double bestDist = point.getDistance(centroids[0]);
            for (int c = 1; c < centroids.length; c++) {
                double dist = point.getDistance(centroids[c]);
                if (dist < bestDist) {
                    bestDist = dist;
                    bestCluster = c;
                }
            }
            labels[i] = bestCluster;
        }
        return labels;
    }
}

