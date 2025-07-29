package com.wonnabe.product.service;

import com.wonnabe.product.domain.UserSavingsVO;
import com.wonnabe.product.domain.SavingsProductVO;
import com.wonnabe.product.dto.TransactionSummaryDto; // 변경된 DTO 임포트
import com.wonnabe.product.dto.MonthlyChartDto;
import com.wonnabe.product.dto.UserSavingsDetailResponseDto;
import com.wonnabe.product.mapper.UserSavingsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class UserSavingsService {

    private final UserSavingsMapper userSavingsMapper;

    public UserSavingsDetailResponseDto getSavingsDetail(String userId, Long productId) {
        UserSavingsVO userSavings = userSavingsMapper.findSavingsDetailByIds(userId, productId);

        if (userSavings == null) {
            // 혹은 throw new CustomNotFoundException("해당 상품을 찾을 수 없습니다.");
            return null;
        }

        SavingsProductVO product = userSavings.getSavingsProduct();

        List<MonthlyChartDto> monthlyChart = new ArrayList<>();
        Integer finalAchievementRate = 0;
                long calculatedCurrentBalance = 0L;
        List<TransactionSummaryDto> monthlySumsForBalance = userSavingsMapper.findMonthlyTransactionSums(userId, userSavings.getStartDate());
        for (TransactionSummaryDto summary : monthlySumsForBalance) {
            calculatedCurrentBalance += summary.getTotalSavings();
        }
        userSavings.setCurrentBalance(calculatedCurrentBalance);

        // 적금 상품일경우
        if (userSavings.getMonthlyPayment() != null && userSavings.getMonthlyPayment() > 0) {
            monthlyChart = createMonthlyChart(userId, userSavings);
            if (!monthlyChart.isEmpty()) {
                finalAchievementRate = monthlyChart.get(monthlyChart.size() - 1).getPercentage();
            }
        } else {
            finalAchievementRate = 100;
        }

        return UserSavingsDetailResponseDto.from(userSavings, product, monthlyChart, finalAchievementRate);
    }

    private List<MonthlyChartDto> createMonthlyChart(String userId, UserSavingsVO userSavings) {
        List<TransactionSummaryDto> monthlySumsFromDb = userSavingsMapper.findMonthlyTransactionSums(userId, userSavings.getStartDate());

        Map<String, Long> monthlySumsMap = monthlySumsFromDb.stream()
                .collect(Collectors.toMap(TransactionSummaryDto::getMonth, TransactionSummaryDto::getTotalSavings));

        List<MonthlyChartDto> chartDataList = new ArrayList<>();
        long cumulativeExpectedAmount = 0;
        long cumulativeActualAmount = 0;

        LocalDate startDate = userSavings.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate today = LocalDate.now();

        for (LocalDate date = startDate.withDayOfMonth(1); !date.isAfter(today); date = date.plusMonths(1)) {
            String monthKey = date.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            String monthDisplay = date.format(DateTimeFormatter.ofPattern("M월"));

            long monthlySavings = monthlySumsMap.getOrDefault(monthKey, 0L);

            cumulativeActualAmount += monthlySavings;
            cumulativeExpectedAmount += userSavings.getMonthlyPayment();

            int achievementRate = 0;
            if (cumulativeExpectedAmount > 0) {
                achievementRate = (int) (((double) cumulativeActualAmount / cumulativeExpectedAmount) * 100);
            }

            chartDataList.add(new MonthlyChartDto(monthDisplay, Math.min(achievementRate, 100)));
        }

        return chartDataList;
    }
}