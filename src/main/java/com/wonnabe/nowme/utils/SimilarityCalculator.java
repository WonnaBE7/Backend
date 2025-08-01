package com.wonnabe.nowme.utils;

/**
 * 벡터 유사도 계산 유틸리티
 */
public class SimilarityCalculator {

    /**
     * 코사인 유사도 계산 (0~1)
     */
    public static double cosineSimilarity(double[] vector1, double[] vector2) {
        if (vector1 == null || vector2 == null) {
            throw new IllegalArgumentException("Vectors cannot be null");
        }
        if (vector1.length != vector2.length) {
            throw new IllegalArgumentException("Vectors must have the same length");
        }

        double dotProduct = 0.0;
        double magnitude1 = 0.0;
        double magnitude2 = 0.0;

        for (int i = 0; i < vector1.length; i++) {
            dotProduct += vector1[i] * vector2[i];
            magnitude1 += vector1[i] * vector1[i];
            magnitude2 += vector2[i] * vector2[i];
        }

        if (magnitude1 == 0 || magnitude2 == 0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(magnitude1) * Math.sqrt(magnitude2));
    }

    /**
     * 유클리드 거리 계산
     */
    public static double euclideanDistance(double[] vector1, double[] vector2) {
        if (vector1 == null || vector2 == null) {
            throw new IllegalArgumentException("Vectors cannot be null");
        }
        if (vector1.length != vector2.length) {
            throw new IllegalArgumentException("Vectors must have the same length");
        }

        double sum = 0.0;
        for (int i = 0; i < vector1.length; i++) {
            double diff = vector1[i] - vector2[i];
            sum += diff * diff;
        }

        return Math.sqrt(sum);
    }

    /**
     * 거리 기반 유사도 (1 / (1 + distance))
     */
    public static double euclideanSimilarity(double[] vector1, double[] vector2) {
        double distance = euclideanDistance(vector1, vector2);
        return 1.0 / (1.0 + distance);
    }

    /**
     * 맨해튼 거리 계산 (L1 norm)
     */
    public static double manhattanDistance(double[] vector1, double[] vector2) {
        if (vector1 == null || vector2 == null) {
            throw new IllegalArgumentException("Vectors cannot be null");
        }
        if (vector1.length != vector2.length) {
            throw new IllegalArgumentException("Vectors must have the same length");
        }

        double sum = 0.0;
        for (int i = 0; i < vector1.length; i++) {
            sum += Math.abs(vector1[i] - vector2[i]);
        }

        return sum;
    }
}