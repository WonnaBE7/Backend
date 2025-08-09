package com.wonnabe.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class UserDetailResponse {
    private final int code;
    private final String message;
    private final UserDetailData data;

    @Builder
    public UserDetailResponse(int code, String message, UserDetailData data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    @Getter
    public static class UserDetailData {
        private final String userId;

        // 보험 추천시 필요 (tinyint -> Integer)
        private final Integer lifestyleSmoking;
        private final Integer lifestyleFamilyMedical;
        private final Integer lifestyleBeforeDiseases;
        private final Integer lifestyleExerciseFreq;
        private final Integer lifestyleAlcoholFreq;

        // 예적금 추천시 필요
        private final String incomeSourceType;
        private final String incomeEmploymentStatus;

        // 나우미설정시 필요
        private final Integer householdSize;
        private final String incomeJobType;

        public UserDetailData() {
            this.userId = null;
            this.lifestyleSmoking = null;
            this.lifestyleFamilyMedical = null;
            this.lifestyleBeforeDiseases = null;
            this.lifestyleExerciseFreq = null;
            this.lifestyleAlcoholFreq = null;
            this.incomeSourceType = null;
            this.incomeEmploymentStatus = null;
            this.householdSize = null;
            this.incomeJobType = null;
        }

        @Builder
        public UserDetailData(String userId, Integer lifestyleSmoking, Integer lifestyleFamilyMedical,
                              Integer lifestyleBeforeDiseases, Integer lifestyleExerciseFreq,
                              Integer lifestyleAlcoholFreq, String incomeSourceType,
                              String incomeEmploymentStatus, Integer householdSize, String incomeJobType) {
            this.userId = userId;
            this.lifestyleSmoking = lifestyleSmoking;
            this.lifestyleFamilyMedical = lifestyleFamilyMedical;
            this.lifestyleBeforeDiseases = lifestyleBeforeDiseases;
            this.lifestyleExerciseFreq = lifestyleExerciseFreq;
            this.lifestyleAlcoholFreq = lifestyleAlcoholFreq;
            this.incomeSourceType = incomeSourceType;
            this.incomeEmploymentStatus = incomeEmploymentStatus;
            this.householdSize = householdSize;
            this.incomeJobType = incomeJobType;
        }
    }
}