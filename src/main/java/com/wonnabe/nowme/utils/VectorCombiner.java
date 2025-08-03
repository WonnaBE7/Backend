package com.wonnabe.nowme.utils;

/**
 * 벡터 결합 유틸리티
 * 정량 벡터와 정성 벡터를 가중 평균으로 결합
 */
public class VectorCombiner {

    /**
     * 두 벡터를 가중 평균으로 결합
     */
    public static double[] combine(double[] quantVector, double[] qualVector, double quantWeight, double qualWeight) {
        if (quantVector == null || qualVector == null) {
            throw new IllegalArgumentException("Vectors cannot be null");
        }
        if (quantVector.length != qualVector.length) {
            throw new IllegalArgumentException("Vectors must have the same length");
        }
        if (Math.abs(quantWeight + qualWeight - 1.0) > 0.0001) {
            throw new IllegalArgumentException("Weights must sum to 1.0");
        }

        double[] combined = new double[quantVector.length];
        for (int i = 0; i < combined.length; i++) {
            combined[i] = (quantVector[i] * quantWeight) + (qualVector[i] * qualWeight);
        }

        return combined;
    }

    /**
     * 기본 가중치 6:4로 결합
     */
    public static double[] combine(double[] quantVector, double[] qualVector) {
        return combine(quantVector, qualVector, 0.6, 0.4);
    }

    /**
     * 벡터 평균 계산
     */
    public static double[] average(double[]... vectors) {
        if (vectors == null || vectors.length == 0) {
            throw new IllegalArgumentException("At least one vector is required");
        }

        int length = vectors[0].length;
        double[] average = new double[length];

        for (double[] vector : vectors) {
            if (vector.length != length) {
                throw new IllegalArgumentException("All vectors must have the same length");
            }
            for (int i = 0; i < length; i++) {
                average[i] += vector[i];
            }
        }

        for (int i = 0; i < length; i++) {
            average[i] /= vectors.length;
        }

        return average;
    }
}