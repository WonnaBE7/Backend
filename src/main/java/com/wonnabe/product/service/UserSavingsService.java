package com.wonnabe.product.service;

import com.wonnabe.product.domain.SavingsProductVO;
import com.wonnabe.product.domain.UserSavingsVO;
import com.wonnabe.product.dto.MonthlyChartDto;
import com.wonnabe.product.dto.TransactionSummaryDto;
import com.wonnabe.product.dto.UserSavingsDetailResponseDto;
import com.wonnabe.product.mapper.UserSavingsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional(readOnly = true)
public class UserSavingsService {

    private final UserSavingsMapper userSavingsMapper;

    public UserSavingsDetailResponseDto getSavingsDetail(String userId, Long productId) {
        UserSavingsVO userSavings = userSavingsMapper.findSavingsDetailByIds(userId, productId);
        if (userSavings == null) {
            return null;
        }

        List<TransactionSummaryDto> monthlyTransactions = userSavingsMapper.findMonthlyTransactionSums(userId, userSavings.getStartDate());

        // 이자 계산을 제외한 순수 납입 원금의 합계를 계산합니다.
        long currentBalanceWithoutInterest = monthlyTransactions.stream().mapToLong(TransactionSummaryDto::getTotalSavings).sum();
        userSavings.setCurrentBalance(currentBalanceWithoutInterest);

        // 최종 달성률(%)을 계산합니다.
        Integer finalAchievementRate = calculateFinalAchievementRate(userSavings);

        // 이자를 포함한 최근 5개월의 자산 추이 차트를 생성합니다.
        List<MonthlyChartDto> monthlyAssetChart = createMonthlyAssetChart(userSavings, monthlyTransactions);

        return UserSavingsDetailResponseDto.from(userSavings, userSavings.getSavingsProduct(), monthlyAssetChart, finalAchievementRate);
    }

    /**
     * 상품 유형에 따라 최종 달성률(%)을 계산합니다.
     */
    private Integer calculateFinalAchievementRate(UserSavingsVO userSavings) {
        boolean isSavingsProduct = userSavings.getMonthlyPayment() != null && userSavings.getMonthlyPayment() > 0;
        long currentPrincipal = userSavings.getCurrentBalance(); // 현재까지 납입한 총 원금

        if (isSavingsProduct) {
            // 적금: (현재 총 납입 원금 / 만기 시 총 납입해야 할 원금) * 100
            LocalDate startDate = toLocalDate(userSavings.getStartDate());
            LocalDate maturityDate = toLocalDate(userSavings.getMaturityDate());
            long totalMonths = Period.between(startDate, maturityDate).toTotalMonths();
            long totalExpectedPrincipal = userSavings.getMonthlyPayment() * totalMonths;
            return (totalExpectedPrincipal == 0) ? 100 : (int) (((double) currentPrincipal / totalExpectedPrincipal) * 100);
        } else {
            // 예금: (현재 총 납입 원금 / 최초 계약 원금) * 100
            long initialPrincipal = userSavings.getPrincipalAmount();
            return (initialPrincipal == 0) ? 100 : (int) (((double) currentPrincipal / initialPrincipal) * 100);
        }
    }

    /**
     * 이자를 포함한 월별 누적 자산 추이 차트를 생성하고, 최근 5개월치 데이터만 반환합니다.
     */
    private List<MonthlyChartDto> createMonthlyAssetChart(UserSavingsVO userSavings, List<TransactionSummaryDto> monthlyTransactions) {
        Map<String, Long> monthlySumsMap = monthlyTransactions.stream()
                .collect(Collectors.toMap(TransactionSummaryDto::getMonth, TransactionSummaryDto::getTotalSavings));

        List<MonthlyChartDto> fullChartData = new ArrayList<>();
        LocalDate startDate = toLocalDate(userSavings.getStartDate());
        LocalDate today = LocalDate.now();

        long cumulativePrincipal = 0; // 누적 원금
        long cumulativeInterest = 0;  // 누적 이자
        double yearlyRate = userSavings.getSavingsProduct().getBaseRate() / 100.0; // 연이율
        double monthlyRate = yearlyRate / 12.0; // 월이율

        for (LocalDate date = startDate.withDayOfMonth(1); !date.isAfter(today); date = date.plusMonths(1)) {
            // 해당 월의 이자는, 이전 달까지의 누적 원금을 기준으로 계산됩니다.
            cumulativeInterest += (long) (cumulativePrincipal * monthlyRate);

            // 이번 달 납입액을 더하여 새로운 누적 원금을 만듭니다.
            cumulativePrincipal += monthlySumsMap.getOrDefault(date.format(DateTimeFormatter.ofPattern("yyyy-MM")), 0L);

            // 차트에는 (누적 원금 + 누적 이자)를 담습니다.
            long totalAsset = cumulativePrincipal + cumulativeInterest;
            fullChartData.add(new MonthlyChartDto(date.format(DateTimeFormatter.ofPattern("M월")), totalAsset));
        }

        // 전체 기간의 데이터 중 마지막 5개를 반환합니다.
        if (fullChartData.size() <= 5) {
            return fullChartData;
        } else {
            return fullChartData.subList(fullChartData.size() - 5, fullChartData.size());
        }
    }

    private LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}