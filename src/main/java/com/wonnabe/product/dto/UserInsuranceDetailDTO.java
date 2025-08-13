package com.wonnabe.product.dto;

import com.wonnabe.product.domain.InsuranceProductVO;
import com.wonnabe.product.domain.UserInsuranceVO;
import lombok.Builder;
import lombok.Data;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.List;

/**
 * 보유 보험 상품 상세 조회 API의 최종 응답 형식을 정의하는 DTO입니다.
 * 서비스 계층에서 계산된 모든 데이터를 조합하여 클라이언트에게 전달될 최종 형태를 만듭니다.
 */
@Data
@Builder
public class UserInsuranceDetailDTO {

    private String productId;
    private String insuranceName;
    private String insuranceCompany;

    private String startDate;
    private String maturityDate;
    private String term; // 납입 기간

    private String currentAmount; // 현재까지 납입한 총액
    private String getAmount; // 만기 시 예상 수령액 (또는 보장액)
    private String achievementRate; // 달성률

    private List<MonthlyChartDto> monthlyChart;

    /**
     * 서비스 계층에서 조회한 VO와 계산된 데이터를 조합하여 DTO를 생성하는 정적 팩토리 메서드입니다.
     *
     * @param userInsurance   사용자 보험 정보 VO
     * @param product         보험 상품 정보 VO
     * @param monthlyChart    월별 차트 DTO 리스트
     * @param achievementRate 서비스에서 계산한 최종 달성률
     * @param getAmount       서비스에서 계산한 예상 수령액
     * @return UserInsuranceDetailDTO 인스턴스
     */
    public static UserInsuranceDetailDTO from(UserInsuranceVO userInsurance,
                                              InsuranceProductVO product,
                                              List<MonthlyChartDto> monthlyChart,
                                              String achievementRate,
                                              String getAmount) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM");

        // 가입 기간 계산
        String term = "";
        if (userInsurance.getStartDate() != null && userInsurance.getEndDate() != null) {
            LocalDate start = userInsurance.getStartDate().toLocalDate(); // Use toLocalDate() directly
            LocalDate end = userInsurance.getEndDate().toLocalDate();     // Use toLocalDate() directly
            Period period = Period.between(start, end);
            int months = period.getYears() * 12 + period.getMonths();
            term = months + "개월";
        }

        return UserInsuranceDetailDTO.builder()
                .productId(String.valueOf(product.getProductId()))
                .insuranceName(product.getProductName())
                .insuranceCompany(product.getProviderName())
                .startDate(dateFormat.format(userInsurance.getStartDate()))
                .maturityDate(dateFormat.format(userInsurance.getEndDate()))
                .term(term)
                .currentAmount(String.valueOf(userInsurance.getTotalPaid())) // UserInsuranceVO의 totalPaid 사용
                .getAmount(getAmount) // 서비스 계층에서 계산 필요
                .achievementRate(achievementRate) // 서비스 계층에서 계산 필요
                .monthlyChart(monthlyChart) // 서비스 계층에서 생성 필요
                .build();
    }
}
