package com.wonnabe.asset.service;

import com.wonnabe.asset.dto.CategorySummaryDTO;
import com.wonnabe.asset.mapper.SummariesCacheMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ConsumptionSummaryService {

    @Autowired
    private SummariesCacheMapper cacheMapper;

    // 월 별 소비 금액
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

    // 월 별 카테고리별 소비 비율
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

    // 예상 월 소비 및 오늘의 소비
    public Map<String, Object> getOverviewConsumption(String userId) {
        LocalDate today = LocalDate.now();
        String yearMonth = today.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        // 이번 달 현재까지 소비금액
        Double monthToDate = cacheMapper.getMonthlyTotalConsumption(userId, yearMonth);
        double totalSoFar = monthToDate != null ? monthToDate : 0;

        // 하루 평균으로 예상 월 소비 계산
        int dayOfMonth = today.getDayOfMonth();
        int daysInMonth = today.lengthOfMonth();
        double estimatedMonthly = dayOfMonth > 0 ? (totalSoFar / dayOfMonth) * daysInMonth : 0;

        // 오늘의 소비
        Double todayTotal = cacheMapper.getTodayConsumption(userId, today.toString());
        double todayConsumption = todayTotal != null ? todayTotal : 0;

        Map<String, Object> estimatedMap = new LinkedHashMap<>();
        estimatedMap.put("amount", Math.round(estimatedMonthly));
        estimatedMap.put("calculatedUntil", today.toString());

        Map<String, Object> todayMap = new LinkedHashMap<>();
        todayMap.put("amount", todayConsumption);
        todayMap.put("calculatedDate", today.toString());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("estimatedMonthlyConsumption", estimatedMap);
        result.put("todayConsumption", todayMap);

        return result;
    }

    // 오늘의 소비 카테고리별 소비 금액 및 어제와 비교
    public Map<String, Object> getTodayCategoryConsumption(String userId) {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        // 오늘과 어제의 카테고리별 소비 금액 리스트 가져오기
        List<CategorySummaryDTO> todayList = cacheMapper.getDailyCategorySummary(userId, today.toString());
        List<CategorySummaryDTO> yesterdayList = cacheMapper.getDailyCategorySummary(userId, yesterday.toString());

        double totalToday = todayList.stream().mapToDouble(CategorySummaryDTO::getAmount).sum();

        // 어제 데이터 매핑 (카테고리별 비교용)
        Map<String, Double> yesterdayMap = yesterdayList.stream()
                .collect(Collectors.toMap(CategorySummaryDTO::getConsumptionCategory, CategorySummaryDTO::getAmount));

        // 퍼센트와 diffFromYesterday 계산
        List<Map<String, Object>> categories = todayList.stream()
                .map(c -> {
                    double percentage = totalToday > 0 ? (c.getAmount() / totalToday * 100) : 0;
                    double yesterdayAmount = yesterdayMap.getOrDefault(c.getConsumptionCategory(), 0.0);
                    double diffYesterday = c.getAmount() - yesterdayAmount;

                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("consumptionCategory", c.getConsumptionCategory());
                    map.put("amount", c.getAmount());
                    map.put("percentage", Math.round(percentage * 10) / 10.0);
                    map.put("diffFromYesterday", diffYesterday);
                    return map;
                })
                .collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("calculatedDate", today.toString());
        result.put("categories", categories);

        return result;
    }
}
