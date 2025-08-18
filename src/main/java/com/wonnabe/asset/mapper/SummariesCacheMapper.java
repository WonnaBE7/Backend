package com.wonnabe.asset.mapper;

import com.wonnabe.asset.dto.CategorySummaryDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface SummariesCacheMapper {

    // 메인 페이지 소비내역 요약
    Double getMainConsumption(@Param("userId") String userId, @Param("yearMonth") String yearMonth);
    Double getMainConsumptionLastMonth(@Param("userId") String userId, @Param("yearMonth") String lastMonth);


    // 월별 총 소비금액
    Double getMonthlyTotalConsumption(@Param("userId") String userId,
                                      @Param("yearMonth") String yearMonth);

    // 카테고리별 월별 합계
    List<CategorySummaryDTO> getMonthlyCategorySummary(@Param("userId") String userId,
                                                       @Param("yearMonth") String yearMonth);

    // 예상월소비 - 지난달 소비 평균
    Double getAvgMonthlyConsumption(@Param("userId") String userId,
                                    @Param("startYm") String startYm,
                                    @Param("endYm") String endYm);

    // 해당 월의 카테고리별 실제 금액
    List<Map<String, Object>> getCategoryAmountsByMonth(
            @Param("userId") String userId,
            @Param("yearMonth") String yearMonth);

    // 기간(시작~끝) 내 월별 카테고리 합을 만든 뒤, 카테고리별 평균 (요청월 제외 범위 전달)
    List<Map<String, Object>> getCategoryAvgInRange(
            @Param("userId") String userId,
            @Param("startYm") String startYm,
            @Param("endYm") String endYm);


    // 오늘의 소비
    Double getTodayConsumption(@Param("userId") String userId, @Param("today") String today);

    // 오늘/어제 카테고리별 소비 집계
    List<CategorySummaryDTO> getDailyCategorySummary(@Param("userId") String userId, @Param("date") String date);
}
