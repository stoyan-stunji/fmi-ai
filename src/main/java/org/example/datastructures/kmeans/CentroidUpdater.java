package org.example.datastructures.kmeans;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import java.util.Random;

public class CentroidUpdater {

    public RealVector[] updateCentroids(RealMatrix data, int[] labels, int k, Random rand) {
        int nFeatures = data.getColumnDimension();
        RealVector[] sums = initializeZeroVectors(k, nFeatures);
        int[] counts = new int[k];
        computeCentroidSums(data, labels, sums, counts);
        return finalizeCentroids(data, sums, counts, k, rand);
    }

    private void computeCentroidSums(RealMatrix data, int[] labels, RealVector[] sums, int[] counts) {
        for (int i = 0; i < data.getRowDimension(); i++) {
            int label = labels[i];
            sums[label] = sums[label].add(new ArrayRealVector(data.getRow(i)));
            counts[label]++;
        }
    }

    private RealVector[] finalizeCentroids(RealMatrix data, RealVector[] sums, int[] counts, int k, Random rand) {
        RealVector[] centroids = new RealVector[k];
        for (int i = 0; i < k; i++) {
            if (counts[i] > 0) {
                centroids[i] = sums[i].mapDivide(counts[i]);
            } else {
                int randomIndex = rand.nextInt(data.getRowDimension());
                centroids[i] = new ArrayRealVector(data.getRow(randomIndex));
            }
        }
        return centroids;
    }

    private RealVector[] initializeZeroVectors(int k, int nFeatures) {
        RealVector[] vectors = new RealVector[k];
        for (int i = 0; i < k; i++) {
            vectors[i] = new ArrayRealVector(nFeatures);
        }
        return vectors;
    }
}
