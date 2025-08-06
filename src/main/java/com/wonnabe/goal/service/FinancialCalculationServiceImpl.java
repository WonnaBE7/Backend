package com.wonnabe.goal.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class FinancialCalculationServiceImpl implements FinancialCalculationService {

    @Override
    public BigDecimal calculateMonthlyDepositSimple(BigDecimal targetAmount, int periodMonths, BigDecimal annualRate) {
        BigDecimal t = new BigDecimal(periodMonths);
        BigDecimal r12 = annualRate.divide(new BigDecimal("12"), 10, RoundingMode.HALF_UP);
        BigDecimal interestPart = r12.multiply(t.multiply(t.add(BigDecimal.ONE)).divide(new BigDecimal("2"), 10, RoundingMode.HALF_UP));
        BigDecimal denominator = t.add(interestPart);

        return denominator.compareTo(BigDecimal.ZERO) == 0 ?
                BigDecimal.ZERO : targetAmount.divide(denominator, 0, RoundingMode.CEILING);
    }

    @Override
    public BigDecimal calculateMonthlyDepositCompound(BigDecimal targetAmount, int periodMonths, BigDecimal annualRate) {
        BigDecimal i = annualRate.divide(new BigDecimal("12"), 10, RoundingMode.HALF_UP);

        if (i.compareTo(BigDecimal.ZERO) == 0) {
            return targetAmount.divide(new BigDecimal(periodMonths), 0, RoundingMode.CEILING);
        }

        BigDecimal numerator = targetAmount.multiply(i);
        BigDecimal denominator = (BigDecimal.ONE.add(i)).pow(periodMonths).subtract(BigDecimal.ONE);

        return denominator.compareTo(BigDecimal.ZERO) == 0 ?
                BigDecimal.ZERO : numerator.divide(denominator, 0, RoundingMode.CEILING);
    }

    @Override
    public BigDecimal calculatePrincipalSimple(BigDecimal targetAmount, int periodMonths, BigDecimal annualRate) {
        BigDecimal nYears = new BigDecimal(periodMonths).divide(new BigDecimal("12"), 10, RoundingMode.HALF_UP);
        BigDecimal denominator = BigDecimal.ONE.add(annualRate.multiply(nYears));

        return denominator.compareTo(BigDecimal.ZERO) == 0 ?
                BigDecimal.ZERO : targetAmount.divide(denominator, 0, RoundingMode.CEILING);
    }

    @Override
    public BigDecimal calculatePrincipalCompound(BigDecimal targetAmount, int periodMonths, BigDecimal annualRate) {
        double nYears = (double) periodMonths / 12.0;
        BigDecimal denominator = BigDecimal.valueOf(Math.pow(BigDecimal.ONE.add(annualRate).doubleValue(), nYears));

        return denominator.compareTo(BigDecimal.ZERO) == 0 ?
                BigDecimal.ZERO : targetAmount.divide(denominator, 0, RoundingMode.CEILING);
    }

    @Override
    public BigDecimal calculateFinalAmountForSaving(BigDecimal monthlyDeposit, int months, BigDecimal rate, String rateType) {
        if ("단리".equals(rateType)) {
            return calculateSavingFinalAmountSimple(monthlyDeposit, months, rate);
        } else {
            return calculateSavingFinalAmountCompound(monthlyDeposit, months, rate);
        }
    }

    @Override
    public BigDecimal calculateFinalAmountForDeposit(BigDecimal principal, int months, BigDecimal rate, String rateType) {
        double nYears = (double) months / 12.0;

        if ("단리".equals(rateType)) {
            return principal.multiply(BigDecimal.ONE.add(rate.multiply(new BigDecimal(nYears))));
        } else {
            return principal.multiply(BigDecimal.valueOf(Math.pow(BigDecimal.ONE.add(rate).doubleValue(), nYears)));
        }
    }

    @Override
    public BigDecimal calculateAchievementRate(BigDecimal expectedAmount, BigDecimal targetAmount) {
        if (targetAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal rate = expectedAmount.divide(targetAmount, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));

        return rate.min(BigDecimal.valueOf(100));
    }

    private BigDecimal calculateSavingFinalAmountSimple(BigDecimal monthlyDeposit, int months, BigDecimal rate) {
        BigDecimal t = new BigDecimal(months);
        BigDecimal r12 = rate.divide(new BigDecimal("12"), 10, RoundingMode.HALF_UP);
        BigDecimal principal = monthlyDeposit.multiply(t);
        BigDecimal interest = monthlyDeposit.multiply(r12)
                .multiply(t.multiply(t.add(BigDecimal.ONE)).divide(new BigDecimal("2")));
        return principal.add(interest);
    }

    private BigDecimal calculateSavingFinalAmountCompound(BigDecimal monthlyDeposit, int months, BigDecimal rate) {
        BigDecimal i = rate.divide(new BigDecimal("12"), 10, RoundingMode.HALF_UP);

        if (i.compareTo(BigDecimal.ZERO) == 0) {
            return monthlyDeposit.multiply(new BigDecimal(months));
        }

        BigDecimal factor = (BigDecimal.ONE.add(i)).pow(months).subtract(BigDecimal.ONE)
                .divide(i, 10, RoundingMode.HALF_UP);
        return monthlyDeposit.multiply(factor);
    }
}
