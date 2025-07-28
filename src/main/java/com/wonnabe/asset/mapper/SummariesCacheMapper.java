package com.wonnabe.asset.mapper;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SummariesCacheMapper {
    // 특정 연월의 총 소비금액 (캐시 테이블 기준)
    Double getMonthlyTotalConsumption(@Param("userId") String userId, @Param("yearMonth") String yearMonth);
}
