package com.wonnabe.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailRequest {
    private String userId;

    // 보험 추천시 필요 (tinyint로 변경)
    private Integer lifestyleSmoking;           // 0 또는 1
    private Integer lifestyleFamilyMedical;     // 0 또는 1
    private Integer lifestyleBeforeDiseases;    // 0 또는 1
    private Integer lifestyleExerciseFreq;      // 0 또는 1
    private Integer lifestyleAlcoholFreq;       // 0 또는 1

    // 예적금 추천시 필요 (새로 추가)
    private String incomeSourceType;            // "근로소득", "사업소득", "기타소득"
    private String incomeEmploymentStatus;      // "재직", "휴직", "퇴직"

    // 나우미설정시 필요 (기존)
    private Integer householdSize;              // 가구원 수
    private String incomeJobType;               // 직업 유형
}