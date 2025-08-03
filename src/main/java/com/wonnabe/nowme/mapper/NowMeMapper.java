package com.wonnabe.nowme.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Set;

@Mapper
public interface NowMeMapper {

    // 총 소비 금액 (최근 1개월)
    double getTotalSpending(@Param("userId") Long userId);

    // 필수소비 카테고리들의 소비 합계
    double getSpendingByCategories(@Param("userId") Long userId, @Param("categories") Set<String> categories);

    // 가구원 수
    int getHouseholdSize(@Param("userId") Long userId);

    // 최근 1개월 선택소비 카테고리 종류 수
    int getMonthlySelectableCategoryCount(@Param("userId") Long userId);

    // 연소득
    double getAnnualIncome(@Param("userId") Long userId);

    // 최근 1개월 주별 소비 표준편차
    double getWeeklySpendingStdDev(@Param("userId") Long userId);
}