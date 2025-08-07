package com.wonnabe.nowme.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 사용자 진단 결과 벡터
 * NowMe의 4개 축 진단 점수를 담는 도메인 객체
 */
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserVector {

    private double financialActivity;    // 사용자의 금융활동성 점수 (0~1)
    private double consumptionPattern;   // 사용자의 소비패턴 점수 (0~1)
    private double planningStyle;        // 사용자의 계획방식 점수 (0~1)
    private double riskTendency;         // 사용자의 리스크성향 점수 (0~1)

    /**
     * 점수 배열로부터 UserVector 생성
     * @param vector [금융활동성, 소비패턴, 계획방식, 리스크성향] 순서의 배열
     */
    public UserVector(double[] vector) {
        if (vector == null || vector.length != 4) {
            throw new IllegalArgumentException("Vector must be a non-null array of length 4");
        }
        this.financialActivity = vector[0];
        this.consumptionPattern = vector[1];
        this.planningStyle = vector[2];
        this.riskTendency = vector[3];
    }

    /**
     * UserVector를 배열로 변환
     * @return [금융활동성, 소비패턴, 계획방식, 리스크성향] 순서의 배열
     */
    // 배열 형태로 변환 → 유사도 계산 등에 사용
    public double[] toArray() {
        return new double[]{financialActivity, consumptionPattern, planningStyle, riskTendency};
    }

    /**
     * 벡터의 크기(magnitude) 계산
     * @return 유클리드 노름값
     */
    public double magnitude() {
        return Math.sqrt(
                financialActivity * financialActivity +
                        consumptionPattern * consumptionPattern +
                        planningStyle * planningStyle +
                        riskTendency * riskTendency
        );
    }
}