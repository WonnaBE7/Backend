package com.wonnabe.asset.controller;

import com.wonnabe.asset.dto.CategorySummaryDTO;
import com.wonnabe.asset.service.ConsumptionSummaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assets/consumption")
public class ConsumptionSummaryController {

    @Autowired
    private ConsumptionSummaryService summaryService;

    /**
     * 월별 소비금액 조회 (이번 달 vs 지난 달 등)
     * @param yearMonth 조회할 연월 (예: 2025-07)
     * @param userId (실제는 JWT에서 추출, 테스트용으로 허용)
     */
    @GetMapping("/monthly")
    public Map<String, Object> getMonthlyConsumption(
            @RequestParam("yearMonth") String yearMonth,
            @RequestParam(value = "userId", required = false) String userId) {

        if (userId == null) {
            userId = "111e2222-aaaa-bbbb-cccc-123456789000"; // 테스트용
        }

        Map<String, Object> data = summaryService.getMonthlyConsumption(userId, yearMonth);

        return Map.of(
                "code", 200,
                "message", "월별 소비 금액 조회 성공",
                "data", data
        );
    }
    // 추가: 카테고리별 비율
    @GetMapping("/categories")
    public Map<String, Object> getMonthlyCategorySummary(
            @RequestParam("yearMonth") String yearMonth,
            @RequestParam(value = "userId", required = false) String userId) {

        if (userId == null) {
            userId = "111e2222-aaaa-bbbb-cccc-123456789000"; // 테스트용
        }

        List<CategorySummaryDTO> categories = summaryService.getMonthlyCategorySummary(userId, yearMonth);
        return Map.of(
                "code", 200,
                "message", "월별 카테고리 소비비율 조회 성공",
                "data", Map.of(
                        "yearMonth", yearMonth,
                        "categories", categories
                )
        );
    }
    @GetMapping("/categories/view")
    public String viewCategories(@RequestParam("yearMonth") String yearMonth,
                                 @RequestParam(value = "userId", required = false) String userId,
                                 Model model) {
        if (userId == null) {
            userId = "111e2222-aaaa-bbbb-cccc-123456789000"; // 테스트용
        }

        // 카테고리별 데이터 (Service 호출)
        List<CategorySummaryDTO> categories = summaryService.getMonthlyCategorySummary(userId, yearMonth);

        model.addAttribute("yearMonth", yearMonth);
        model.addAttribute("categories", categories);

        return "categories"; // categories.jsp 렌더링
    }
}
