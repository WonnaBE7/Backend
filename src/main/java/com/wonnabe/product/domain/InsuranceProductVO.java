package com.wonnabe.product.domain;

import lombok.*;

import java.math.BigDecimal;

/**
 * InsuranceProduct 테이블과 매핑되는 VO 클래스
 * 예적금 상품의 기본 정보를 담는 객체
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsuranceProductVO {
    private Long productId;
    private String providerName;
    private String productName;

    private Integer minAge;
    private Integer maxAge;
    private BigDecimal femalePremium;
    private BigDecimal malePremium;

    private String coverageType;
    private String coverageDesc;
    private String coverageLimit;
    private String note;
    private String myMoney;

    // Recommend를 위한 Score
    private Float priceCompetitivenessScore; // 가격 경쟁력 점수
    private Float coverageLimitScore; // 보장 한도 점수
    private Float coverageScopeScore; // 보장 범위 점수
    private Float deductibleScore; // 자기 부담금 점수
    private Float refundScopeScore; // 환급 범위 점수
}