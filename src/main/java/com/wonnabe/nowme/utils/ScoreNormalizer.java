package com.wonnabe.nowme.utils;

/**
 * 점수 정규화 유틸리티
 * 다양한 범위의 점수를 0~5 범위로 정규화
 */
public class ScoreNormalizer {

    /**
     * 전체 배열을 0~5 범위로 정규화
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
                result[i] = 2.5;
            }
            return result;
        }

        double[] normalized = new double[scores.length];
        for (int i = 0; i < scores.length; i++) {
            normalized[i] = ((scores[i] - min) / (max - min)) * 5.0;
        }

        return normalized;
    }

    /**
     * 단일 값을 정규화 (예: 0~5)
     */
    public static double normalize(double value, double min, double max, double targetMin, double targetMax) {
        if (max == min) {
            return (targetMin + targetMax) / 2.0;
        }
        return targetMin + ((value - min) / (max - min)) * (targetMax - targetMin);
    }
}