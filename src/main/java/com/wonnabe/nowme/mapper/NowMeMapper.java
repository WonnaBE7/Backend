package com.wonnabe.nowme.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Set;

@Mapper
public interface NowMeMapper {

    // 🔹 [SpendingEvaluator] 소비패턴 정량 평가용

    // 총 소비 금액 (최근 1개월)
    double getTotalSpending(@Param("userId") String userId);

    // 필수소비 카테고리들의 소비 합계 (food, transport 등)
    double getSpendingByCategories(@Param("userId") String userId, @Param("categories") Set<String> categories);

    // 가구원 수
    int getHouseholdSize(@Param("userId") String userId);

    // 최근 1개월 선택소비 카테고리 종류 수
    int getMonthlySelectableCategoryCount(@Param("userId") String userId);

    // 연소득
    double getAnnualIncome(@Param("userId") String userId);

    // 최근 1개월 주별 소비 표준편차
    double getWeeklySpendingStdDev(@Param("userId") String userId);

    // 🔹 [ActivityEvaluator] 금융활동성 정량 평가용

    // 계좌 유형 수 (입출금, 투자, 연금, 기타 등)
    int getAccountCategoryCount(@Param("userId") String userId);

    // 저축상품 가입 수 (예금, 적금 등)
    int getSavingsProductCount(@Param("userId") String userId);

    // 보험상품 가입 수
    int getInsuranceProductCount(@Param("userId") String userId);

    // 최근 1개월 거래 건수
    int getMonthlyTransactionCount(@Param("userId") String userId);

    // 최근 1개월 서로 다른 소비처(MCC) 수
    int getMonthlyMerchantCategoryCount(@Param("userId") String userId);

    // 🔹 [RiskEvaluator] 리스크성향 정량 평가용

    // 전체 계좌 잔액 합계
    double getTotalBalance(@Param("userId") String userId);

    // 특정 카테고리 계좌들의 잔액 합계 (예: 투자, 주식, 펀드 등)
    double getBalanceByCategories(@Param("userId") String userId, @Param("categories") Set<String> categories);

    // 특정 카테고리 계좌 수 (예: 투자상품의 개수)
    int getAccountCountByCategories(@Param("userId") String userId, @Param("categories") Set<String> categories);

    // 가입한 저축상품의 평균 max_rate
    double getAvgSavingsRate(@Param("userId") String userId);

    // 🔹 [PlanningEvaluator] 계획방식 정량 평가용

    // 목표 관리 관련
    int getGoalCount(@Param("userId") String userId);
    double getAverageGoalProgressRate(@Param("userId") String userId);

    // 저축 계획 관련
    double getPlannedMonthlySaving(@Param("userId") String userId);
    double getActualMonthlySaving(@Param("userId") String userId);

    // 소비 안정성 관련
    double getMonthlySpendingStdDev(@Param("userId") String userId);
    double getMonthlySpendingAverage(@Param("userId") String userId);

    // 🔹 [진단 결과 저장용]

    // 진단 이력 저장
    void insertDiagnosisHistory(
            @Param("userId") String userId,
            @Param("nowmeId") Integer nowmeId,
            @Param("similarity") Double similarity,
            @Param("userVector") String userVector
    );

    // User_Info의 nowme_id 업데이트
    void updateUserNowmeId(
            @Param("userId") String userId,
            @Param("nowmeId") Integer nowmeId
    );
}