package com.wonnabe.product.domain;

import lombok.Data;

/**
 * 보험 상품의 상세 정보를 담는 VO(Value Object) 클래스.
 * insurance_product 테이블의 데이터를 나타냅니다.
 * 보유 상품 조회, 추천 등 다양한 곳에서 재사용.
 */
@Data
public class InsuranceProductVO {

    private Long productId;
    private String providerName;
    private String productName;
    private Integer minAge;
    private Integer maxAge;

    private Integer femalePremium;
    private Integer malePremium;

    private String coverageType;
    private String coverageDesc;
    private String coverageLimit;

    private String note;
    private String myMoney;

    private Integer scorePriceCompetitiveness;
    private Integer scoreCoverageLimit;
    private Integer scoreCoverageScope;
    private Integer scoreDeductibleLevel;
    private Integer scoreRefundScope;
}
