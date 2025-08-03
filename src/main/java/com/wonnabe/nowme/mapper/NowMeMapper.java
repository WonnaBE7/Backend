package com.wonnabe.nowme.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Set;

@Mapper
public interface NowMeMapper {

    // 🔹 [SpendingEvaluator] 소비패턴 정량 평가용

    // 총 소비 금액 (최근 1개월)
    double getTotalSpending(@Param("userId") Long userId);

    // 필수소비 카테고리들의 소비 합계 (food, transport 등)
    double getSpendingByCategories(@Param("userId") Long userId, @Param("categories") Set<String> categories);

    // 가구원 수
    int getHouseholdSize(@Param("userId") Long userId);

    // 최근 1개월 선택소비 카테고리 종류 수
    int getMonthlySelectableCategoryCount(@Param("userId") Long userId);

    // 연소득
    double getAnnualIncome(@Param("userId") Long userId);

    // 최근 1개월 주별 소비 표준편차
    double getWeeklySpendingStdDev(@Param("userId") Long userId);


    // 🔹 [ActivityEvaluator] 금융활동성 정량 평가용

    // 계좌 유형 수 (입출금, 투자, 연금, 기타 등)
    int getAccountCategoryCount(@Param("userId") Long userId);

    // 저축상품 가입 수 (예금, 적금 등)
    int getSavingsProductCount(@Param("userId") Long userId);

    // 보험상품 가입 수
    int getInsuranceProductCount(@Param("userId") Long userId);

    // 최근 1개월 거래 건수
    int getMonthlyTransactionCount(@Param("userId") Long userId);

    // 최근 1개월 서로 다른 소비처(MCC) 수
    int getMonthlyMerchantCategoryCount(@Param("userId") Long userId);


    // 🔹 [RiskEvaluator] 리스크성향 정량 평가용

    // 전체 계좌 잔액 합계
    double getTotalBalance(@Param("userId") Long userId);

    // 특정 카테고리 계좌들의 잔액 합계 (예: 투자, 주식, 펀드 등)
    double getBalanceByCategories(@Param("userId") Long userId, @Param("categories") Set<String> categories);

    // 특정 카테고리 계좌 수 (예: 투자상품의 개수)
    int getAccountCountByCategories(@Param("userId") Long userId, @Param("categories") Set<String> categories);

    // 가입한 저축상품의 평균 max_rate
    double getAvgSavingsRate(@Param("userId") Long userId);


    // 🔹 [PlanningEvaluator] 계획방식 정량 평가용 (향후 확장)

    // 목표 설정 개수
    // int getGoalCount(@Param("userId") Long userId);

    // 평균 목표 진척률 (progress_rate 평균)
    // double getAverageGoalProgressRate(@Param("userId") Long userId);


    // 🔹 [진단 결과 저장용] (향후 확장)

    // 진단 결과 저장 (DiagnosisHistory insert용)
    // void insertDiagnosisHistory(@Param("history") com.wonnabe.nowme.domain.DiagnosisHistory history);
}