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
        private final Boolean lifestyleSmoking;
        private final Boolean lifestyleDrinking;
        private final Boolean lifestyleExercise;
        private final Integer householdSize;
        private final Boolean lifestyleFamilyMedical;
        private final Boolean lifestyleBeforeDiseases;
        private final String incomeJobType;

        public UserDetailData() {
            this.userId = null;
            this.lifestyleSmoking = null;
            this.lifestyleDrinking = null;
            this.lifestyleExercise = null;
            this.householdSize = null;
            this.lifestyleFamilyMedical = null;
            this.lifestyleBeforeDiseases = null;
            this.incomeJobType = null;
        }

        public UserDetailData(String userId, Long lifestyleSmoking, Long lifestyleDrinking,
                              Long lifestyleExercise, Integer householdSize, Long lifestyleFamilyMedical,
                              Long lifestyleBeforeDiseases, String incomeJobType) {
            this.userId = userId;
            this.lifestyleSmoking = lifestyleSmoking != null && lifestyleSmoking == 1;
            this.lifestyleDrinking = lifestyleDrinking != null && lifestyleDrinking == 1;
            this.lifestyleExercise = lifestyleExercise != null && lifestyleExercise == 1;
            this.householdSize = householdSize;
            this.lifestyleFamilyMedical = lifestyleFamilyMedical != null && lifestyleFamilyMedical == 1;
            this.lifestyleBeforeDiseases = lifestyleBeforeDiseases != null && lifestyleBeforeDiseases == 1;
            this.incomeJobType = incomeJobType;
        }

        @Builder
        public UserDetailData(String userId, Boolean lifestyleSmoking, Boolean lifestyleDrinking,
                              Boolean lifestyleExercise, Integer householdSize, Boolean lifestyleFamilyMedical,
                              Boolean lifestyleBeforeDiseases, String incomeJobType) {
            this.userId = userId;
            this.lifestyleSmoking = lifestyleSmoking;
            this.lifestyleDrinking = lifestyleDrinking;
            this.lifestyleExercise = lifestyleExercise;
            this.householdSize = householdSize;
            this.lifestyleFamilyMedical = lifestyleFamilyMedical;
            this.lifestyleBeforeDiseases = lifestyleBeforeDiseases;
            this.incomeJobType = incomeJobType;
        }
    }
}