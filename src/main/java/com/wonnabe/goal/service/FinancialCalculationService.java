package com.wonnabe.goal.service;

import java.math.BigDecimal;

public interface FinancialCalculationService {

    /**
     * 적금 단리 방식의 월 납입액 계산
     * 목표 금액을 달성하기 위해 매월 납입해야 할 금액을 단리 계산으로 산출
     *
     * @param targetAmount 목표 금액 (원)
     * @param periodMonths 저축 기간 (개월)
     * @param annualRate   연 이자율 (소수점 형태, 예: 0.05 = 5%)
     * @return 월 납입 필요 금액 (원)
     */
    public BigDecimal calculateMonthlyDepositSimple(BigDecimal targetAmount, int periodMonths, BigDecimal annualRate);

    /**
     * 적금 복리 방식의 월 납입액 계산
     * 목표 금액을 달성하기 위해 매월 납입해야 할 금액을 복리 계산으로 산출
     *
     * @param targetAmount 목표 금액 (원)
     * @param periodMonths 저축 기간 (개월)
     * @param annualRate   연 이자율 (소수점 형태, 예: 0.05 = 5%)
     * @return 월 납입 필요 금액 (원)
     */
    public BigDecimal calculateMonthlyDepositCompound(BigDecimal targetAmount, int periodMonths, BigDecimal annualRate);

    /**
     * 예금 단리 방식에서 목표 금액 달성을 위한 필요 원금을 계산
     * 단리 이자 계산으로 목표 금액을 만들기 위해 처음에 예치해야 할 원금 산출
     *
     * @param targetAmount 목표 금액 (원)
     * @param periodMonths 예치 기간 (개월)
     * @param annualRate   연 이자율 (소수점 형태, 예: 0.05 = 5%)
     * @return 필요 원금 (원)
     */
    public BigDecimal calculatePrincipalSimple(BigDecimal targetAmount, int periodMonths, BigDecimal annualRate);

    /**
     * 예금 복리 방식에서 목표 금액 달성을 위한 필요 원금을 계산
     * 복리 이자 계산으로 목표 금액을 만들기 위해 처음에 예치해야 할 원금 산출
     *
     * @param targetAmount 목표 금액 (원)
     * @param periodMonths 예치 기간 (개월)
     * @param annualRate   연 이자율 (소수점 형태, 예: 0.05 = 5%)
     * @return 필요 원금 (원)
     */
    public BigDecimal calculatePrincipalCompound(BigDecimal targetAmount, int periodMonths, BigDecimal annualRate);

    /**
     * 적금의 만기 시 예상 수령액을 계산
     * 월 납입액, 기간, 이자율, 이자 계산 방식을 바탕으로 만기 시 받을 수 있는 총 금액 산출
     *
     * @param monthlyDeposit 월 납입액 (원)
     * @param months         저축 기간 (개월)
     * @param rate           연 이자율 (소수점 형태, 예: 0.05 = 5%)
     * @param rateType       이자 계산 방식 ("단리" 또는 "복리")
     * @return 만기 시 예상 수령액 (원)
     */
    public BigDecimal calculateFinalAmountForSaving(BigDecimal monthlyDeposit, int months, BigDecimal rate, String rateType);

    /**
     * 예금의 만기 시 예상 수령액을 계산
     * 원금, 기간, 이자율, 이자 계산 방식을 바탕으로 만기 시 받을 수 있는 총 금액을 산출
     *
     * @param principal 예치 원금 (원)
     * @param months    예치 기간 (개월)
     * @param rate      연 이자율 (소수점 형태, 예: 0.05 = 5%)
     * @param rateType  이자 계산 방식 ("단리" 또는 "복리")
     * @return 만기 시 예상 수령액 (원)
     */
    public BigDecimal calculateFinalAmountForDeposit(BigDecimal principal, int months, BigDecimal rate, String rateType);

    /**
     * 목표 금액 대비 예상 달성률을 계산
     * 예상 수령액이 목표 금액을 얼마나 달성하는지 백분율로 계산, 100%를 초과하지 않도록 제한
     *
     * @param expectedAmount 예상 수령액 (원)
     * @param targetAmount   목표 금액 (원)
     * @return 목표 달성률 (백분율, 0~100)
     */
    public BigDecimal calculateAchievementRate(BigDecimal expectedAmount, BigDecimal targetAmount);
}
