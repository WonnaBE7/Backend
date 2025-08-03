package com.wonnabe.nowme.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NowMeResponseDTO {

    private boolean success;          // 저장 성공 여부
    private String personaName;       // 예: "자린고비형"

    // 디버깅용 점수들
    private Double activityScore;     // 금융활동성 점수
    private Double spendingScore;     // 소비패턴 점수
    private Double planningScore;     // 계획방식 점수
    private Double riskScore;         // 리스크성향 점수
    private Double similarity;        // 유사도

    public static NowMeResponseDTO success(String personaName) {
        return new NowMeResponseDTO(true, personaName, null, null, null, null, null);
    }

    // 점수들까지 포함한 성공 응답
    public static NowMeResponseDTO successWithScores(String personaName,
                                                     double activityScore,
                                                     double spendingScore,
                                                     double planningScore,
                                                     double riskScore,
                                                     double similarity) {
        return new NowMeResponseDTO(true, personaName, activityScore, spendingScore, planningScore, riskScore, similarity);
    }

    public static NowMeResponseDTO failure() {
        return new NowMeResponseDTO(false, null, null, null, null, null, null);
    }
}