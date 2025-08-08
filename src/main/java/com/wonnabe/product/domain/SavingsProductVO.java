package com.wonnabe.product.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Savings_Product 테이블과 매핑되는 VO 클래스
 * 예적금 상품의 기본 정보를 담는 객체
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavingsProductVO {

    private Long productId;         // 상품 ID (PK)
    private String productName;     // 상품명
    private String bankName;        // 금융기관

    private Float baseRate;         // 기본금리
    private Float maxRate;          // 최고금리
    private Long minAmount;         // 최소한도
    private Long maxAmount;         // 최대한도
    private Integer minJoinPeriod;  // 최소가입기간 (개월)
    private Integer maxJoinPeriod;  // 최대가입기간 (개월)
    private String rateType;        // 이자 유형 (단리, 복리)

    // recommend를 위한 Score
    private int scoreInterestRate;      // 금리_점수
    private int scoreInterestType;  // 단복리_점수
    private int scorePreferentialCondition;      // 우대조건_점수
    private int scoreCancelBenefit;          // 중도해지페널티_점수
    private int scoreMaxAmount;            // 최대한도_점수



}