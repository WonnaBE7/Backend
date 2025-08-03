package com.wonnabe.nowme.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Set;

@Mapper
public interface NowMeMapper {

    // 🔹 [SpendingEvaluator] 소비 정량 평가용

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


//    // 🔹 [진단 결과 저장용]
//
//    // 진단 결과 저장 (DiagnosisHistory insert용)
//    void insertDiagnosisHistory(@Param("history") com.wonnabe.nowme.domain.DiagnosisHistory history);
}