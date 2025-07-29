package com.wonnabe.asset.controller;

import com.wonnabe.asset.dto.CategorySummaryDTO;
import com.wonnabe.asset.service.ConsumptionSummaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
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

        // 순서를 보장하려면 LinkedHashMap 사용
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("code", 200);
        response.put("message", "월별 소비 금액 조회 성공");
        response.put("data", data);

        return response;
    }

    /**
     * 월별 카테고리별 소비 비율 조회
     * @param yearMonth 조회할 연월 (예: 2025-07)
     * @param userId (실제는 JWT에서 추출, 테스트용으로 허용)
     */
    @GetMapping("/categories")
    public Map<String, Object> getMonthlyCategorySummary(
            @RequestParam("yearMonth") String yearMonth,
            @RequestParam(value = "userId", required = false) String userId) {

        if (userId == null) {
            userId = "111e2222-aaaa-bbbb-cccc-123456789000"; // 테스트용
        }

        List<CategorySummaryDTO> categories = summaryService.getMonthlyCategorySummary(userId, yearMonth);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("yearMonth", yearMonth);
        data.put("categories", categories);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("code", 200);
        response.put("message", "월별 카테고리 소비비율 조회 성공");
        response.put("data", data);

        return response;
    }
    /**
     * 예상 월 소비 및 오늘의 소비
     * @param userId (실제는 JWT에서 추출, 테스트용으로 허용)
     */
    @GetMapping("/overview")
    public Map<String, Object> getOverviewConsumption(
            @RequestParam(value = "userId", required = false) String userId) {

        if (userId == null) {
            userId = "111e2222-aaaa-bbbb-cccc-123456789000"; // 테스트용
        }

        Map<String, Object> data = summaryService.getOverviewConsumption(userId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("code", 200);
        response.put("message", "월 예상 소비 및 오늘의 소비 조회 성공");
        response.put("data", data);

        return response;
    }

    /**
     * 오늘의 소비 카테고리별 소비 금액 및 어제와 비교
     * @param userId (실제는 JWT에서 추출, 테스트용으로 허용)
     */
    @GetMapping("/today/categories")
    public Map<String, Object> getTodayCategoryConsumption(
            @RequestParam(value = "userId", required = false) String userId) {

        if (userId == null) {
            userId = "111e2222-aaaa-bbbb-cccc-123456789000"; // 테스트용
        }

        Map<String, Object> data = summaryService.getTodayCategoryConsumption(userId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("code", 200);
        response.put("message", "오늘의 소비 카테고리 비율 조회 성공");
        response.put("data", data);

        return response;
    }


    /**
     * JSP 뷰 렌더링 (테스트용)
     */
    @GetMapping("/categories/view")
    public String viewCategories(@RequestParam("yearMonth") String yearMonth,
                                 @RequestParam(value = "userId", required = false) String userId,
                                 Model model) {
        if (userId == null) {
            userId = "111e2222-aaaa-bbbb-cccc-123456789000"; // 테스트용
        }

        List<CategorySummaryDTO> categories = summaryService.getMonthlyCategorySummary(userId, yearMonth);

        model.addAttribute("yearMonth", yearMonth);
        model.addAttribute("categories", categories);

        return "categories"; // categories.jsp 렌더링
    }
}
