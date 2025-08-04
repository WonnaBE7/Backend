package com.wonnabe.product.service;

import com.wonnabe.product.domain.UserInsuranceVO;
import com.wonnabe.product.dto.MonthlyChartDto;
import com.wonnabe.product.dto.TransactionSummaryDto;
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

        // 월별 보험 거래 내역 조회 (최근 12개월 또는 보험 시작일부터)
        // 차트 시작 날짜를 현재로부터 11개월 전의 1일로 설정
        LocalDate chartStartMonth = LocalDate.now().minusMonths(11).withDayOfMonth(1);
        // 보험 시작일이 차트 시작일보다 늦으면 보험 시작일을 차트 시작일로 설정
        if (userInsuranceVO.getStartDate().toLocalDate().isAfter(chartStartMonth)) {
            chartStartMonth = userInsuranceVO.getStartDate().toLocalDate().withDayOfMonth(1);
        }

        List<TransactionSummaryDto> monthlyTransactions = userInsuranceMapper.findMonthlyTransactionSums(userId, java.sql.Date.valueOf(chartStartMonth));

        // 월별 차트 데이터 생성
        List<MonthlyChartDto> monthlyChart = createMonthlyInsuranceChart(userInsuranceVO, monthlyTransactions, chartStartMonth);

        // 조회된 VO 객체를 DTO로 변환하여 반환합니다.
        // DTO의 정적 메소드를 사용하여 변환 로직을 위임합니다.
        UserInsuranceDetailDTO dto = UserInsuranceDetailDTO.from(userInsuranceVO);
        dto.setMonthlyChart(monthlyChart); // Set the monthly chart data
        return dto;
    }

    @Override
    public String existsById(Long productId) {
        return "";
    }

    /**
     * 월별 보험 자산 추이 차트 데이터를 생성합니다.
     * @param userInsuranceVO 사용자의 보험 가입 정보
     * @param monthlyTransactions 월별 보험 거래 요약 목록
     * @param chartStartMonth 차트의 시작 월
     * @return 월별 차트 데이터 목록
     */
    private List<MonthlyChartDto> createMonthlyInsuranceChart(UserInsuranceVO userInsuranceVO, List<TransactionSummaryDto> monthlyTransactions, LocalDate chartStartMonth) {
        List<MonthlyChartDto> fullChartData = new ArrayList<>();
        LocalDate currentDate = LocalDate.now();

        // Map monthly transactions for easy lookup
        Map<String, Long> monthlyTransactionMap = monthlyTransactions.stream()
                .collect(Collectors.toMap(TransactionSummaryDto::getMonth, TransactionSummaryDto::getTotalSavings));

        BigDecimal cumulativeAmount = BigDecimal.ZERO;

        for (LocalDate date = chartStartMonth; !date.isAfter(currentDate); date = date.plusMonths(1)) {
            String monthKey = date.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            Long monthlyTransactionAmount = monthlyTransactionMap.getOrDefault(monthKey, 0L);

            cumulativeAmount = cumulativeAmount.add(BigDecimal.valueOf(monthlyTransactionAmount));

            fullChartData.add(new MonthlyChartDto(date.format(DateTimeFormatter.ofPattern("M월")), cumulativeAmount.longValue()));
        }

        return fullChartData;
    }
}
