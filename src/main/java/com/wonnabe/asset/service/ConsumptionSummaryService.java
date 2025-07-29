package com.wonnabe.asset.service;

import com.wonnabe.asset.dto.CategorySummaryDTO;
import com.wonnabe.asset.mapper.SummariesCacheMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConsumptionSummaryService {

    @Autowired
    private SummariesCacheMapper cacheMapper;

    public Map<String, Object> getMonthlyConsumption(String userId, String yearMonth) {
        Double totalAmount = cacheMapper.getMonthlyTotalConsumption(userId, yearMonth);
        String calculatedUntil = LocalDate.now().toString();

        Map<String, Object> monthToDateConsumption = new LinkedHashMap<>();
        monthToDateConsumption.put("amount", totalAmount != null ? totalAmount : 0);
        monthToDateConsumption.put("calculatedUntil", calculatedUntil);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("yearMonth", yearMonth);
        result.put("monthToDateConsumption", monthToDateConsumption);

        return result;
    }

    // 카테고리별 소비 비율
    public List<CategorySummaryDTO> getMonthlyCategorySummary(String userId, String yearMonth) {
        List<CategorySummaryDTO> rawList = cacheMapper.getMonthlyCategorySummary(userId, yearMonth);

        double totalAmount = rawList.stream().mapToDouble(CategorySummaryDTO::getAmount).sum();

        // 각 항목 비율만 계산해서 DTO에 세팅
        for (CategorySummaryDTO c : rawList) {
            double percentage = totalAmount > 0 ? (c.getAmount() / totalAmount * 100) : 0;
            c.setPercentage(Math.round(percentage * 10) / 10.0);  // 소수점 1자리
        }

        return rawList;
    }

}
