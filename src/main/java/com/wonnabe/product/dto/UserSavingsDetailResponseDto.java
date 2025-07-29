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
 * 예적금 상품 상세 조회 API 응답용 DTO
 * 데이터 가공 및 형식 변환 로직을 포함
 */
@Data
@Builder
public class UserSavingsDetailResponseDto {

    private String productId;
    private String productName;
    private String bankName;
    private String productType;         // "예금" 또는 "적금"
    private String startDate;           // "yyyy-MM-dd"
    private String maturityDate;        // "yyyy-MM-dd"
    private String term;                // "12개월"
    private Long currentAmount;
    private Integer achievementRate;
    private List<MonthlyChartDto> monthlyChart;

    /**
     * 서비스 레이어에서 조회한 VO와 계산된 데이터를 조합하여 DTO를 생성하는 팩토리 메서드
     * @param userSavings 사용자 저축 정보 VO
     * @param product 상품 정보 VO
     * @param monthlyChart 월별 차트 DTO 리스트
     * @param achievementRate 서비스에서 계산한 최종 달성률
     * @return UserSavingsDetailResponseDto 인스턴스
     */
    public static UserSavingsDetailResponseDto from(UserSavingsVO userSavings,
                                                    SavingsProductVO product,
                                                    List<MonthlyChartDto> monthlyChart,
                                                    Integer achievementRate) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        // 1. 상품 타입 판별 로직 (UserSavingsDetailVO -> DTO)
        String productType = (userSavings.getMonthlyPayment() != null && userSavings.getMonthlyPayment() > 0) ? "적금" : "예금";

        // 2. 가입 기간 계산 로직 (UserSavingsDetailVO -> DTO)
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
                .productType(productType)
                .startDate(dateFormat.format(userSavings.getStartDate()))
                .maturityDate(dateFormat.format(userSavings.getMaturityDate()))
                .term(term)
                .currentAmount(userSavings.getCurrentBalance()) // 현재 잔액은 서비스에서 계산 후 VO에 설정
                .achievementRate(achievementRate)
                .monthlyChart(monthlyChart)
                .build();
    }
}