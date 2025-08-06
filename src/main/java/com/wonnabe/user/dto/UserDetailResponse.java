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
        private final String user_id;
        private final Boolean lifestyle_smoking;
        private final Boolean lifestyle_drinking;
        private final Boolean lifestyle_exercise;
        private final Integer household_size;
        private final Boolean lifestyle_family_medical;
        private final Boolean lifestyle_before_diseases;
        private final String income_job_type;

        // 기본 생성자 (MyBatis용)
        public UserDetailData() {
            this.user_id = null;
            this.lifestyle_smoking = null;
            this.lifestyle_drinking = null;
            this.lifestyle_exercise = null;
            this.household_size = null;
            this.lifestyle_family_medical = null;
            this.lifestyle_before_diseases = null;
            this.income_job_type = null;
        }

        // MyBatis가 DB에서 Long 타입으로 받아오는 값을 Boolean으로 변환하는 생성자
        public UserDetailData(String user_id, Long lifestyle_smoking, Long lifestyle_drinking,
                              Long lifestyle_exercise, Integer household_size, Long lifestyle_family_medical,
                              Long lifestyle_before_diseases, String income_job_type) {
            this.user_id = user_id;
            this.lifestyle_smoking = lifestyle_smoking != null && lifestyle_smoking == 1;
            this.lifestyle_drinking = lifestyle_drinking != null && lifestyle_drinking == 1;
            this.lifestyle_exercise = lifestyle_exercise != null && lifestyle_exercise == 1;
            this.household_size = household_size;
            this.lifestyle_family_medical = lifestyle_family_medical != null && lifestyle_family_medical == 1;
            this.lifestyle_before_diseases = lifestyle_before_diseases != null && lifestyle_before_diseases == 1;
            this.income_job_type = income_job_type;
        }

        // Builder용 생성자 (Boolean 타입 그대로 받음)
        @Builder
        public UserDetailData(String user_id, Boolean lifestyle_smoking, Boolean lifestyle_drinking,
                              Boolean lifestyle_exercise, Integer household_size, Boolean lifestyle_family_medical,
                              Boolean lifestyle_before_diseases, String income_job_type) {
            this.user_id = user_id;
            this.lifestyle_smoking = lifestyle_smoking;
            this.lifestyle_drinking = lifestyle_drinking;
            this.lifestyle_exercise = lifestyle_exercise;
            this.household_size = household_size;
            this.lifestyle_family_medical = lifestyle_family_medical;
            this.lifestyle_before_diseases = lifestyle_before_diseases;
            this.income_job_type = income_job_type;
        }
    }
}