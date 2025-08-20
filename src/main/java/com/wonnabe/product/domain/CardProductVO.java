package com.wonnabe.product.domain;

import com.wonnabe.product.domain.enums.CardType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardProductVO {
    private Long productId; // 카드 ID
    private String cardName; // 카드 명
    private String cardCompany; // 카드사
    private CardType cardType; // 카드의 종류 - (신용, 체크)
    private String benefitSummary; // 혜택 요약
    private String benefitCategories; // 혜택 카테고리
    private String benefitLimit; // 혜택 한도
    private Long performanceCondition; // 실적 조건
    private String annualFeeDomestic; // 국내전용 연회비
    private String annualFeeOverseas; // 해외겸용 연회비
    private String performanceConditionDescription; // 실적 한도 글로
    private Long benefitLimitAmount; // 실적 한도 숫자
    private String mainCategories; // 카드 혜택 main 5개로 요약
    private String cardScore; // 카드 점수 - [확장성, 혜택범위, 전월 실적, 활용도, 연회비 부담]
    private String matchedFilters; // 금융성향에 대한 1차 필터링 결과

    // 1차 필터링한 워너비 대상을 리스트로 변환
    public List<Integer> getMatchedFilters() {
        // 예: ["1", "2"] → List<Integer>로 변환
        String ids = matchedFilters.replaceAll("[\\[\\]\\s\"]", ""); // 대괄호, 공백, 쌍따옴표 제거
        return Arrays.stream(ids.split(","))
            .filter(s -> !s.isEmpty())
            .map(Integer::parseInt)
            .collect(Collectors.toList());
    }

    // 카드 점수 리스트로 변환
    public List<Integer> getCardScores() {
        // 예: ["1", "2"] → List<Integer>로 변환
        String ids = cardScore.replaceAll("[\\[\\]\\s\"]", ""); // 대괄호, 공백, 쌍따옴표 제거
        return Arrays.stream(ids.split(","))
            .filter(s -> !s.isEmpty())
            .map(Integer::parseInt)
            .collect(Collectors.toList());
    }

    // 주요 혜택 리스트로 변환
    public List<String> getMainCategories() {
        // 예: ["1", "2"] → List<String>로 변환
        String ids = mainCategories.replaceAll("[\\[\\]\\s\"]", ""); // 대괄호, 공백, 쌍따옴표 제거
        return Arrays.stream(ids.split(","))
            .filter(s -> !s.isEmpty())
            .map(String::toString)
            .collect(Collectors.toList());
    }

    // 카드 타입 라벨 얻기
    public String getCardTypeLabel() {
        // 예: 신용 → 신용카드
        return cardType != null ? cardType.getLabel() + "카드" : "카드";
    }
}

