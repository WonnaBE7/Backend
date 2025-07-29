package com.wonnabe.product.domain;

import com.wonnabe.product.domain.enums.BenefitCategory;
import com.wonnabe.product.domain.enums.CardType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardProductVO {
    private long productId; // 카드 ID
    private String cardName; // 카드 명
    private String issuer; // 카드사
    private CardType cardType; // 카드의 종류 - (신용, 체크)
    private String benefitSummary; // 혜택 요약
    private List<BenefitCategory> benefitCategories; // 혜택 카테고리
    private String benefitLimit; // 혜택 한도
    private String performanceCondition; // 실적 조건
    private String annualFeeDomestic; // 국내전용 연회비
    private String annualFeeOverSeas; // 해외겸용 연회비
}
