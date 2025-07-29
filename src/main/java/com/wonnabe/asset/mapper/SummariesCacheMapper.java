package com.wonnabe.asset.mapper;

import com.wonnabe.asset.dto.CategorySummaryDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface SummariesCacheMapper {
    // 월별 총 소비금액
    Double getMonthlyTotalConsumption(@Param("userId") String userId,
                                      @Param("yearMonth") String yearMonth);

    // 카테고리별 월별 합계
    List<CategorySummaryDTO> getMonthlyCategorySummary(@Param("userId") String userId,
                                                       @Param("yearMonth") String yearMonth);
    // 오늘의 소비
    Double getTodayConsumption(@Param("userId") String userId, @Param("today") String today);

    // 오늘/어제 카테고리별 소비 집계
    List<CategorySummaryDTO> getDailyCategorySummary(@Param("userId") String userId, @Param("date") String date);
}
