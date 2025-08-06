package com.wonnabe.goal.service;

import com.wonnabe.goal.domain.ProductType;
import com.wonnabe.goal.domain.RecommendedProductVO;
import com.wonnabe.goal.dto.GoalCreateRequestDTO;
import com.wonnabe.goal.mapper.GoalMapper;
import com.wonnabe.product.domain.SavingsProductVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductRecommendationServiceImpl implements ProductRecommendationService {

    private final GoalMapper goalMapper;
    private final FinancialCalculationService calculationService;

    @Override
    public List<RecommendedProductVO> calculateRecommendations(GoalCreateRequestDTO request, Long goalId) {
        List<SavingsProductVO> allProducts = goalMapper.getSavingsProductList();
        List<RecommendedProductVO> recommendations = new ArrayList<>();

        for (SavingsProductVO product : allProducts) {
            if (!isEligibleProduct(product, request.getGoalDurationMonths())) {
                continue;
            }

            RecommendedProductVO recommendation = createRecommendation(product, request, goalId);
            if (recommendation != null) {
                recommendations.add(recommendation);
            }
        }

        return recommendations.stream()
                .sorted(Comparator.comparing(RecommendedProductVO::getInterestRate).reversed()
                        .thenComparing(RecommendedProductVO::getSaveAmount))
                .limit(5)
                .collect(Collectors.toList());
    }

    private boolean isEligibleProduct(SavingsProductVO product, int goalDurationMonths) {
        return goalDurationMonths >= product.getMinJoinPeriod();
    }

    private RecommendedProductVO createRecommendation(SavingsProductVO product, GoalCreateRequestDTO request, Long goalId) {
        int actualJoinPeriod = Math.min(request.getGoalDurationMonths(), product.getMaxJoinPeriod());
        BigDecimal maxRate = BigDecimal.valueOf(product.getMaxRate());
        BigDecimal annualRate = maxRate.divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP);

        ProductType type = ProductType.of(product.getProductId());

        if (type == ProductType.SAVINGS) {
            return createSavingsRecommendation(product, request, goalId, actualJoinPeriod, maxRate, annualRate);
        } else if (type == ProductType.DEPOSIT) {
            return createDepositRecommendation(product, request, goalId, actualJoinPeriod, maxRate, annualRate);
        }

        return null;
    }

    private RecommendedProductVO createSavingsRecommendation(SavingsProductVO product, GoalCreateRequestDTO request,
                                                             Long goalId, int actualJoinPeriod, BigDecimal maxRate, BigDecimal annualRate) {
        BigDecimal monthlyDepositAmount = calculateMonthlyDeposit(request.getTargetAmount(), actualJoinPeriod, annualRate, product.getRateType());

        if (!isValidAmount(monthlyDepositAmount, product)) {
            return null;
        }

        BigDecimal expectedTotalAmount = calculationService.calculateFinalAmountForSaving(
                monthlyDepositAmount, actualJoinPeriod, annualRate, product.getRateType()
        );
        BigDecimal achievementRate = calculationService.calculateAchievementRate(expectedTotalAmount, request.getTargetAmount());

        return buildRecommendedProduct(product, goalId, maxRate, achievementRate, monthlyDepositAmount,
                expectedTotalAmount, actualJoinPeriod);
    }

    private RecommendedProductVO createDepositRecommendation(SavingsProductVO product, GoalCreateRequestDTO request,
                                                             Long goalId, int actualJoinPeriod, BigDecimal maxRate, BigDecimal annualRate) {
        BigDecimal initialPrincipal = calculateInitialPrincipal(request.getTargetAmount(), actualJoinPeriod, annualRate, product.getRateType());

        if (!isValidAmount(initialPrincipal, product)) {
            return null;
        }

        BigDecimal expectedTotalAmount = calculationService.calculateFinalAmountForDeposit(
                initialPrincipal, actualJoinPeriod, annualRate, product.getRateType()
        );
        BigDecimal achievementRate = calculationService.calculateAchievementRate(expectedTotalAmount, request.getTargetAmount());

        return buildRecommendedProduct(product, goalId, maxRate, achievementRate, initialPrincipal,
                expectedTotalAmount, actualJoinPeriod);
    }

    private BigDecimal calculateMonthlyDeposit(BigDecimal targetAmount, int period, BigDecimal rate, String rateType) {
        return "단리".equals(rateType)
                ? calculationService.calculateMonthlyDepositSimple(targetAmount, period, rate)
                : calculationService.calculateMonthlyDepositCompound(targetAmount, period, rate);
    }

    private BigDecimal calculateInitialPrincipal(BigDecimal targetAmount, int period, BigDecimal rate, String rateType) {
        return "단리".equals(rateType)
                ? calculationService.calculatePrincipalSimple(targetAmount, period, rate)
                : calculationService.calculatePrincipalCompound(targetAmount, period, rate);
    }

    private boolean isValidAmount(BigDecimal amount, SavingsProductVO product) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        boolean exceedsMax = product.getMaxAmount() != null &&
                amount.compareTo(BigDecimal.valueOf(product.getMaxAmount())) > 0;
        boolean belowMin = product.getMinAmount() != null &&
                amount.compareTo(BigDecimal.valueOf(product.getMinAmount())) < 0;

        return !exceedsMax && !belowMin;
    }

    private RecommendedProductVO buildRecommendedProduct(SavingsProductVO product, Long goalId, BigDecimal maxRate,
                                                         BigDecimal achievementRate, BigDecimal saveAmount,
                                                         BigDecimal expectedTotalAmount, int actualJoinPeriod) {
        return RecommendedProductVO.builder()
                .goalId(goalId)
                .productId(product.getProductId())
                .productName(product.getProductName())
                .bankName(product.getBankName())
                .interestRate(maxRate)
                .achievementRate(achievementRate)
                .saveAmount(saveAmount)
                .expectedAchievementDate(LocalDate.now().plusMonths(actualJoinPeriod))
                .expectedTotalAmount(expectedTotalAmount)
                .build();
    }
}
