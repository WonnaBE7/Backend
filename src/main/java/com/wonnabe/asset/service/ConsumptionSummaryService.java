package com.wonnabe.asset.service;

import com.wonnabe.asset.mapper.SummariesCacheMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
public class ConsumptionSummaryService {

    @Autowired
    private SummariesCacheMapper cacheMapper;

    public Map<String, Object> getMonthlyConsumption(String userId, String yearMonth) {
        // 해당 월의 총 소비금액 (누적)
        Double totalAmount = cacheMapper.getMonthlyTotalConsumption(userId, yearMonth);

        // UI 표시용: 계산 기준일 (오늘 날짜)
        String calculatedUntil = LocalDate.now().toString();

        Map<String, Object> monthToDateConsumption = new HashMap<>();
        monthToDateConsumption.put("amount", totalAmount != null ? totalAmount : 0);
        monthToDateConsumption.put("calculatedUntil", calculatedUntil);

        Map<String, Object> response = new HashMap<>();
        response.put("yearMonth", yearMonth);
        response.put("monthToDateConsumption", monthToDateConsumption);

        return response;
    }
}
