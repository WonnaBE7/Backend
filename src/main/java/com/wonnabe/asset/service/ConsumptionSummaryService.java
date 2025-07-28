package com.wonnabe.asset.service;

import com.wonnabe.asset.dto.CategorySummaryDTO;
import com.wonnabe.asset.mapper.SummariesCacheMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class ConsumptionSummaryService {

    @Autowired
    private SummariesCacheMapper cacheMapper;

    public Map<String, Object> getMonthlyConsumption(String userId, String yearMonth) {
        Double totalAmount = cacheMapper.getMonthlyTotalConsumption(userId, yearMonth);
        String calculatedUntil = LocalDate.now().toString();

        // 총액도 포맷팅된 문자열로 변환
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.KOREA);
        String formattedTotal = nf.format(totalAmount != null ? totalAmount : 0);

        Map<String, Object> monthToDateConsumption = new HashMap<>();
        monthToDateConsumption.put("amount", formattedTotal);  // 문자열로 반환
        monthToDateConsumption.put("calculatedUntil", calculatedUntil);

        Map<String, Object> response = new HashMap<>();
        response.put("yearMonth", yearMonth);
        response.put("monthToDateConsumption", monthToDateConsumption);

        return response;
    }

    // 카테고리별 소비 비율
    public List<CategorySummaryDTO> getMonthlyCategorySummary(String userId, String yearMonth) {
        List<CategorySummaryDTO> categories = cacheMapper.getMonthlyCategorySummary(userId, yearMonth);

        // 총합 계산
        double totalAmount = categories.stream()
                .mapToDouble(c -> {
                    try {
                        return Double.parseDouble(c.getAmount()); // 문자열 → 숫자 변환
                    } catch (Exception e) {
                        return 0;
                    }
                })
                .sum();

        NumberFormat nf = NumberFormat.getNumberInstance(Locale.KOREA);

        for (CategorySummaryDTO c : categories) {
            double amountVal;
            try {
                amountVal = Double.parseDouble(c.getAmount());
            } catch (Exception e) {
                amountVal = 0;
            }
            c.setAmount(nf.format(amountVal)); // 포맷팅된 문자열로 변환
            double percentage = totalAmount > 0 ? (amountVal / totalAmount * 100) : 0;
            c.setPercentage(Math.round(percentage * 10) / 10.0);
        }

        return categories;
    }
}
