package com.wonnabe.product.service;

import com.wonnabe.product.domain.UserInsuranceVO;
import com.wonnabe.product.dto.MonthlyChartDto;
import com.wonnabe.product.domain.InsuranceProductVO;
import com.wonnabe.product.dto.MonthlyInsuranceReceiptDto;
import com.wonnabe.product.dto.UserInsuranceDetailDTO;
import com.wonnabe.product.mapper.UserInsuranceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * {@link UserInsuranceService}의 구현 클래스.
 */

@Log4j2
@Service("UserInsuranceServiceImpl")
@RequiredArgsConstructor
public class UserInsuranceServiceImpl implements UserInsuranceService {

    private final UserInsuranceMapper userInsuranceMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public UserInsuranceDetailDTO getDetailByProductId(String userId, Long productId) {
        // Mapper를 통해 DB에서 사용자의 특정 보험 정보를 조회합니다.
        UserInsuranceVO userInsuranceVO = userInsuranceMapper.findDetailByProductId(userId, productId);

        // 조회된 데이터가 없으면 null을 반환하여, 컨트롤러에서 404 처리를 하도록 합니다.
        if (userInsuranceVO == null) {
            return null;
        }

        // 보험 상품 상세 정보 조회
        InsuranceProductVO insuranceProductVO = userInsuranceMapper.findInsuranceProductById(productId);
        if (insuranceProductVO == null) {
            // 이 경우는 userInsuranceVO가 존재하는데 product가 없는 비정상적인 상황이므로,
            // 적절한 에러 처리 또는 로깅이 필요할 수 있습니다.
            // 현재는 null 반환으로 처리합니다.
            return null;
        }

        // 총 수령액(getAmount) 및 총 납입액(currentAmount) 조회
        Long totalReceiptAmount = userInsuranceMapper.findTotalReceiptAmount(userId, productId);
        Long totalPaymentAmount = userInsuranceMapper.findTotalPaymentAmount(userId, productId);

        // 달성률(achievementRate) 계산
        Integer achievementRate;
        if (totalPaymentAmount != null && totalPaymentAmount > 0 && totalReceiptAmount != null) {
            BigDecimal rate = BigDecimal.valueOf(totalReceiptAmount)
                                        .multiply(BigDecimal.valueOf(100))
                                        .divide(BigDecimal.valueOf(totalPaymentAmount), 0, BigDecimal.ROUND_HALF_UP);
            achievementRate = rate.intValue();
        } else {
            achievementRate = 0;
        }

        // 월별 보험 거래 내역 조회 (최근 5개월)
        // 차트 시작 날짜를 현재로부터 4개월 전의 1일로 설정 (현재 월 포함 5개월)
        LocalDate chartStartMonth = LocalDate.now().minusMonths(4).withDayOfMonth(1);

        List<MonthlyInsuranceReceiptDto> monthlyReceipts = userInsuranceMapper.findMonthlyInsuranceReceipts(userId, productId, java.sql.Date.valueOf(chartStartMonth));

        // 월별 차트 데이터 생성
        List<MonthlyChartDto> monthlyChart = createMonthlyInsuranceChart(monthlyReceipts, chartStartMonth);

        // 조회된 VO 객체와 계산된 데이터를 DTO로 변환하여 반환합니다.
        // DTO의 정적 메소드를 사용하여 변환 로직을 위임합니다.
        UserInsuranceDetailDTO dto = UserInsuranceDetailDTO.from(userInsuranceVO, insuranceProductVO, monthlyChart, achievementRate, String.valueOf(totalReceiptAmount));
        dto.setMonthlyChart(monthlyChart); // Set the monthly chart data
        return dto;
    }

    @Override
    public String existsById(Long productId) {
        return "";
    }

    /**
     * 월별 보험 수령액 차트 데이터를 생성합니다.
     * @param monthlyReceipts 월별 보험 수령액 목록
     * @param chartStartMonth 차트의 시작 월
     * @return 월별 차트 데이터 목록
     */
    private List<MonthlyChartDto> createMonthlyInsuranceChart(List<MonthlyInsuranceReceiptDto> monthlyReceipts, LocalDate chartStartMonth) {
        List<MonthlyChartDto> fullChartData = new ArrayList<>();
        LocalDate currentDate = LocalDate.now();

        // Map monthly receipts for easy lookup
        Map<String, Long> monthlyReceiptMap = monthlyReceipts.stream()
                .collect(Collectors.toMap(MonthlyInsuranceReceiptDto::getMonth, MonthlyInsuranceReceiptDto::getAmount));

        for (LocalDate date = chartStartMonth; !date.isAfter(currentDate); date = date.plusMonths(1)) {
            String monthKey = date.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            Long monthlyAmount = monthlyReceiptMap.getOrDefault(monthKey, 0L);

            fullChartData.add(new MonthlyChartDto(date.format(DateTimeFormatter.ofPattern("M월")), monthlyAmount));
        }

        return fullChartData;
    }
}
