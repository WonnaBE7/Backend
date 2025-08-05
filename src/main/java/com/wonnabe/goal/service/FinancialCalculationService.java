package com.wonnabe.goal.service;

import java.math.BigDecimal;

public interface FinancialCalculationService {
    public BigDecimal calculateMonthlyDepositSimple(BigDecimal targetAmount, int periodMonths, BigDecimal annualRate);

    public BigDecimal calculateMonthlyDepositCompound(BigDecimal targetAmount, int periodMonths, BigDecimal annualRate);

    public BigDecimal calculatePrincipalSimple(BigDecimal targetAmount, int periodMonths, BigDecimal annualRate);

    public BigDecimal calculatePrincipalCompound(BigDecimal targetAmount, int periodMonths, BigDecimal annualRate);

    public BigDecimal calculateFinalAmountForSaving(BigDecimal monthlyDeposit, int months, BigDecimal rate, String rateType);

    public BigDecimal calculateFinalAmountForDeposit(BigDecimal principal, int months, BigDecimal rate, String rateType);

    public BigDecimal calculateAchievementRate(BigDecimal expectedAmount, BigDecimal targetAmount);
}
