package com.wonnabe.nowme.utils;

/**
 * 점수 정규화 유틸리티 (모든 결과는 0~1 범위)
 * [흐름]
 * 1. 최솟값(min), 최댓값(max) 찾음
 * 2. (값 - min) / (max - min) * 5 공식을 통해 정규화
 * 3. 값이 모두 같으면 중간값 0.5로 고정
 */
public class ScoreNormalizer {

    /**
     * 전체 배열을 0~1 범위로 정규화 (Min-Max)
     * 예: [43, 67, 100] → [0.0, 0.48, 1.0]처럼 바꿔줌
     */
    public static double[] normalize(double[] scores) {
        if (scores == null || scores.length == 0) {
            throw new IllegalArgumentException("Scores array cannot be null or empty");
        }

        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;

        for (double score : scores) {
            if (score < min) min = score;
            if (score > max) max = score;
        }

        if (max == min) {
            double[] result = new double[scores.length];
            for (int i = 0; i < result.length; i++) {
                result[i] = 0.5; // 중간값
            }
            return result;
        }

        double[] normalized = new double[scores.length];
        for (int i = 0; i < scores.length; i++) {
            normalized[i] = (scores[i] - min) / (max - min); // 0~1로 정규화
        }

        return normalized;
    }

    /**
     * 단일 값 정규화 (0~1 범위)
     */
    public static double normalize(double value, double min, double max) {
        if (max == min) return 0.5;
        return (value - min) / (max - min);
    }
}