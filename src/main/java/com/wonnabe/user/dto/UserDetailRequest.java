package com.wonnabe.user.dto;

import lombok.Getter;

@Getter
public class UserDetailRequest {
    private String user_id;
    private Boolean lifestyle_smoking;
    private Boolean lifestyle_drinking;
    private Boolean lifestyle_exercise;
    private Integer household_size;
    private Boolean lifestyle_family_medical;
    private Boolean lifestyle_before_diseases;
    private String income_job_type;
}