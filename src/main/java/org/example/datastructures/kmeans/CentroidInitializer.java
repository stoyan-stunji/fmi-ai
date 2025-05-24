package org.example.datastructures.kmeans;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import java.util.Random;

public class CentroidInitializer {
    public RealVector[] initialize(RealMatrix data, int k, Random rand) {
        RealVector[] centroids = new RealVector[k];
        int nSamples = data.getRowDimension();
        for (int i = 0; i < k; i++) {
            int randomIndex = rand.nextInt(nSamples);
            centroids[i] = new ArrayRealVector(data.getRow(randomIndex));
        }
        return centroids;
    }
}

