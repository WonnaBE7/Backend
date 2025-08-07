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
    private Boolean lifestyleSmoking;
    private Boolean lifestyleDrinking;
    private Boolean lifestyleExercise;
    private Integer householdSize;
    private Boolean lifestyleFamilyMedical;
    private Boolean lifestyleBeforeDiseases;
    private String incomeJobType;
}