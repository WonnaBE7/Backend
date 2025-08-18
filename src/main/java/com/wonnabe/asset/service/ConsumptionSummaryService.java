package com.wonnabe.asset.service;

import com.wonnabe.asset.dto.CategorySummaryDTO;
import com.wonnabe.asset.mapper.SummariesCacheMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ConsumptionSummaryService {

    @Autowired
    private SummariesCacheMapper cacheMapper;

    // main page 소비 내역 요약
    public Map<String, Object> getMonthlyOverview(String userId) {
        LocalDate today = LocalDate.now();
        String yearMonth = today.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        String lastMonth = today.minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"));

        // 이번 달, 지난 달 소비금액 가져오기
        Double thisMonth = cacheMapper.getMonthlyTotalConsumption(userId, yearMonth);
        Double prevMonth = cacheMapper.getMonthlyTotalConsumption(userId, lastMonth);

        double current = thisMonth != null ? thisMonth : 0;
        double previous = prevMonth != null ? prevMonth : 0;
        double diff = current - previous;

        double changeRate = previous > 0 ? (diff / previous * 100) : 0;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("monthlyConsumption", current);
        result.put("changeRate", Math.round(changeRate * 10) / 10.0);  // 소수점 1자리
        result.put("changeAmount", diff);

        return result;
    }

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
    public List<Map<String, Object>> getMonthlyCategorySummary(String userId, String yearMonth) {
        List<CategorySummaryDTO> rawList = cacheMapper.getMonthlyCategorySummary(userId, yearMonth);

        double totalAmount = rawList.stream().mapToDouble(CategorySummaryDTO::getAmount).sum();

        // DTO를 Map으로 변환하면서 diffFromYesterday 제거
        return rawList.stream().map(c -> {
            double percentage = totalAmount > 0 ? (c.getAmount() / totalAmount * 100) : 0;

            Map<String, Object> map = new LinkedHashMap<>();
            map.put("consumptionCategory", c.getConsumptionCategory());
            map.put("amount", c.getAmount());
            map.put("percentage", Math.round(percentage * 10) / 10.0);
            map.put("diffFromLastMonth", c.getDiffFromLastMonth());
            // diffFromYesterday는 추가 안 함

            return map;
        }).collect(Collectors.toList());
    }

    // 예상 월 소비 및 오늘의 소비
    public Map<String, Object> getOverviewConsumption(String userId) {
        LocalDate today = LocalDate.now();
        DateTimeFormatter ymFmt = DateTimeFormatter.ofPattern("yyyy-MM");

        // 오늘 소비
        Double todayTotal = cacheMapper.getTodayConsumption(userId, today.toString());
        double todayConsumption = todayTotal != null ? todayTotal : 0.0;

        // 이번 달 누적 소비
        String thisYm = today.format(ymFmt);
        Double monthToDate = cacheMapper.getMonthlyTotalConsumption(userId, thisYm);
        double totalSoFar = monthToDate != null ? monthToDate : 0.0;

        // 최근 12개월 평균 (현재 달 제외)
        int months = 12;
        String endYm   = today.minusMonths(1).format(ymFmt);
        String startYm = today.minusMonths(months).format(ymFmt);
        Double avgMonthly = cacheMapper.getAvgMonthlyConsumption(userId, startYm, endYm);

        // 예상 월 소비
        double estimatedMonthly;
        if (avgMonthly != null) {
            estimatedMonthly = avgMonthly;
        } else {
            int dayOfMonth = today.getDayOfMonth();
            int daysInMonth = today.lengthOfMonth();
            int ANCHOR = 10;
            double denom = Math.max(ANCHOR, dayOfMonth);
            estimatedMonthly = (denom > 0) ? (totalSoFar / denom) * daysInMonth : 0.0;
        }

        // diffAmount = 예상 - 현재까지 소비 (음수 그대로 반환)
        double diffAmount = estimatedMonthly - totalSoFar;

        // 예상 월 소비 맵
        Map<String, Object> estimatedMap = new LinkedHashMap<>();
        estimatedMap.put("amount", Math.round(estimatedMonthly));
        estimatedMap.put("calculatedUntil", today.toString());
        estimatedMap.put("diffAmount", Math.round(diffAmount)); // 음수 포함 그대로 반환

        // 오늘 소비 맵
        Map<String, Object> todayMap = new LinkedHashMap<>();
        todayMap.put("amount", todayConsumption);
        todayMap.put("calculatedDate", today.toString());

        // 최종 결과
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("estimatedMonthlyConsumption", estimatedMap);
        result.put("todayConsumption", todayMap);

        return result;
    }

    // 예상 카테고리별 소비 금액
    public Map<String, Object> getEstimatedCategoryConsumption(String userId, String yearMonth) {
        var ym  = java.time.YearMonth.parse(yearMonth);
        var fmt = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM");

        String targetYm = ym.format(fmt);
        String endYm    = ym.minusMonths(1).format(fmt);  // 전월
        String startYm  = ym.minusMonths(6).format(fmt);  // 전월 기준 6개월 구간 시작

        // 1) DB 조회
        List<Map<String, Object>> curRows = cacheMapper.getCategoryAmountsByMonth(userId, targetYm);
        List<Map<String, Object>> avgRows = cacheMapper.getCategoryAvgInRange(userId, startYm, endYm);

        // 2) alias에 맞춰 안전 변환 (null 카테고리는 "other"로 치환)
        java.util.function.Function<Object, String> catKey = v -> v == null ? "other" : v.toString();

        // 현재월: SELECT consumption_category AS category, SUM(amount) AS amount
        Map<String, Double> curMap = curRows.stream().collect(
                java.util.stream.Collectors.toMap(
                        r -> catKey.apply(r.get("category")),
                        r -> ((Number) r.get("amount")).doubleValue(),
                        (a, b) -> a,                       // 중복 방지
                        java.util.LinkedHashMap::new
                )
        );

        // 평균: SELECT category, AVG(month_amount) AS avgAmount
        Map<String, Double> avgMap = avgRows.stream().collect(
                java.util.stream.Collectors.toMap(
                        r -> catKey.apply(r.get("category")),
                        r -> ((Number) r.get("avgAmount")).doubleValue(),
                        (a, b) -> a,
                        java.util.LinkedHashMap::new
                )
        );

        // 3) 고정 출력 순서
        java.util.List<String> order = java.util.List.of("food", "transport", "shopping", "culture", "other");

        // 4) 결과 구성 (고정 순서대로 생성)
        java.util.List<Map<String, Object>> categories = new java.util.ArrayList<>();
        for (String c : order) {
            double amount = curMap.getOrDefault(c, 0.0);
            double est    = avgMap.getOrDefault(c, 0.0);

            double percentage;
            if (est > 0) {
                percentage = amount * 100.0 / est;
            } else {
                percentage = (amount > 0) ? 100.0 : 0.0;
            }

            Map<String, Object> m = new LinkedHashMap<>();
            m.put("consumptionCategory", c);
            m.put("estimatedAmount", Math.round(est));                        // 정수 반올림
            m.put("amount", Math.round(amount));
            m.put("percentage", Math.round(percentage * 10.0) / 10.0);        // 소수 1자리
            m.put("diffFromEstimate", Math.round(amount - est));
            categories.add(m);
        }

        // 5) 최종 반환
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("yearMonth", targetYm);
        result.put("categories", categories);
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
