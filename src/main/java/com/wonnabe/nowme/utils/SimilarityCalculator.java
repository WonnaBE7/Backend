package com.wonnabe.nowme.utils;

/**
 * 벡터 유사도 계산 유틸리티
 */
public class SimilarityCalculator {

    /**
     * 코사인 유사도 계산 (0~1)
     * - 방향이 얼마나 비슷한지를 봄
     * - 1에 가까울수록 유사한 방향
     * - 공식: 유사도 = (A • B) / (||A|| * ||B||)
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
     * - 두 벡터 간의 직선 거리
     * - 작을수록 유사
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
     * - 유클리드 거리 기반
     * - 거리가 0이면 유사도 1, 거리가 커질수록 0에 가까워짐
     */
    public static double euclideanSimilarity(double[] vector1, double[] vector2) {
        double distance = euclideanDistance(vector1, vector2);
        return 1.0 / (1.0 + distance);
    }

    /**
     * 맨해튼 거리 계산 (L1 norm)
     * - 각 요소의 절댓값 차이의 합으로 거리 계산
     * - |x1 - y1| + |x2 - y2| + ...
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