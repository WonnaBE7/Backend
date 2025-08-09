package com.wonnabe.nowme.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 페르소나 기준 벡터
 * 12개 페르소나별 4축 기준 점수를 담는 도메인 객체
 */
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PersonaVector {

    private String personaName;          // 페르소나 이름 (예: "자린고비형")
    private double financialActivity;    // 금융활동성 기준값
    private double consumptionPattern;   // 소비패턴 기준값
    private double planningStyle;        // 계획방식 기준값
    private double riskTendency;         // 리스크성향 기준값

    /**
     * PersonaVector를 배열로 변환 (계산용)
     * @return [금융활동성, 소비패턴, 계획방식, 리스크성향] 순서의 배열
     */
    // 4개의 점수를 배열로 변환 → 유사도 계산에 사용
    public double[] toArray() {
        return new double[]{financialActivity, consumptionPattern, planningStyle, riskTendency};
    }

    /**
     * 이름 없이 점수만으로 생성하는 헬퍼 생성자
     */
    // vector 배열을 받아 객체 생성하는 헬퍼 생성자
    public PersonaVector(String personaName, double[] vector) {
        if (vector == null || vector.length != 4) {
            throw new IllegalArgumentException("Vector must be a non-null array of length 4");
        }
        this.personaName = personaName;
        this.financialActivity = vector[0];
        this.consumptionPattern = vector[1];
        this.planningStyle = vector[2];
        this.riskTendency = vector[3];
    }
}