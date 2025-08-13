package com.wonnabe.product.dto;

import com.wonnabe.product.domain.SavingsProductVO;
import com.wonnabe.product.domain.UserSavingsVO;
import lombok.Builder;
import lombok.Data;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.List;

/**
 * 보유 예적금 상품 상세 조회 API의 최종 응답 형식을 정의하는 DTO입니다.
 * 서비스 계층에서 계산된 모든 데이터를 조합하여 클라이언트에게 전달될 최종 형태를 만듭니다.
 */
@Data
@Builder
public class UserSavingsDetailResponseDto {

    /** 상품의 고유 ID */
    private String productId;
    private String productName;

    private String bankName;

    private Float baseRate; // 기본 금리

    /** 상품 유형 ("예금" 또는 "적금") */
    private String productType;

    private String startDate;
    private String maturityDate;
    private String term;

    /** 현재까지의 총 잔액 */
    private Long currentAmount;

    /** 최종 달성률 */
    private Integer achievementRate;

    /** 월별 달성률 추이를 나타내는 차트 데이터 리스트 */
    private List<MonthlyChartDto> monthlyChart;

    /**
     * 서비스 계층에서 조회한 VO와 계산된 데이터를 조합하여 DTO를 생성하는 정적 팩토리 메서드입니다.
     * 이 메서드를 통해 DTO의 생성 로직을 한 곳에서 관리할 수 있습니다.
     *
     * @param userSavings     사용자 저축 정보 VO
     * @param product         상품 정보 VO
     * @param monthlyChart    월별 차트 DTO 리스트
     * @param achievementRate 서비스에서 계산한 최종 달성률
     * @return UserSavingsDetailResponseDto 인스턴스
     */
    public static UserSavingsDetailResponseDto from(UserSavingsVO userSavings,
                                                    SavingsProductVO product,
                                                    List<MonthlyChartDto> monthlyChart,
                                                    Integer achievementRate) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        // 1. 상품 타입 판별: 월 납입액(monthlyPayment) 존재 여부로 적금과 예금을 구분합니다.
        String productType = (userSavings.getMonthlyPayment() != null && userSavings.getMonthlyPayment() > 0) ? "적금" : "예금";

        // 2. 가입 기간 계산: 가입일과 만기일의 차이를 계산하여 "N개월" 형태의 문자열로 만듭니다.
        String term = "";
        if (userSavings.getStartDate() != null && userSavings.getMaturityDate() != null) {
            LocalDate start = userSavings.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate end = userSavings.getMaturityDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            Period period = Period.between(start, end);
            int months = period.getYears() * 12 + period.getMonths();
            term = months + "개월";
        }

        return UserSavingsDetailResponseDto.builder()
                .productId(String.valueOf(product.getProductId()))
                .productName(product.getProductName())
                .bankName(product.getBankName())
                .baseRate(product.getBaseRate()) // 기본 금리 추가
                .productType(productType)
                .startDate(dateFormat.format(userSavings.getStartDate()))
                .maturityDate(dateFormat.format(userSavings.getMaturityDate()))
                .term(term)
                .currentAmount(userSavings.getCurrentBalance()) // 서비스에서 계산된 현재 잔액
                .achievementRate(achievementRate)
                .monthlyChart(monthlyChart)
                .build();
    }
}