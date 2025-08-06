package com.wonnabe.user.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class UserInfoResponse {
    private final String userId;
    private final String name;
    private final String email;
    private final String nowME;                    // 현재 진단된 금융성향 이름
    private final List<String> wonnaBE;            // 선택한 워너비 이름들
    private final String job;                      // 직업
    private final Long monthlyIncome;              // 월소득 (연봉/12)

    @Builder
    public UserInfoResponse(String userId, String name, String email, String nowME,
                            List<String> wonnaBE, String job, Long monthlyIncome,
                            Boolean lifestyleSmoking, Boolean lifestyleDrinking, Boolean lifestyleExercise,
                            Integer householdSize, Boolean lifestyleFamilyMedical, Boolean lifestyleBeforeDiseases) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.nowME = nowME;
        this.wonnaBE = wonnaBE;
        this.job = job;
        this.monthlyIncome = monthlyIncome;
    }
}