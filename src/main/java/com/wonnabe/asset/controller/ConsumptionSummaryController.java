package com.wonnabe.asset.controller;

import com.wonnabe.asset.dto.CategorySummaryDTO;
import com.wonnabe.asset.service.ConsumptionSummaryService;
import com.wonnabe.common.util.JsonResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assets/consumption")
public class ConsumptionSummaryController {

    @Autowired
    private ConsumptionSummaryService summaryService;

    /** userId 기본값 처리 */
    private String getUserId(String userId) {
        return (userId != null) ? userId : "111e2222-aaaa-bbbb-cccc-123456789000";
    }

    @GetMapping("/main/overview")
    public ResponseEntity<Object> getMonthlyOverview(@RequestParam(value = "userId", required = false) String userId) {
        Map<String, Object> data = summaryService.getMonthlyOverview(getUserId(userId));
        return JsonResponse.ok("이번달 소비 현황 조회 성공", data);
    }

    @GetMapping("/monthly")
    public ResponseEntity<Object> getMonthlyConsumption(@RequestParam("yearMonth") String yearMonth,
                                                        @RequestParam(value = "userId", required = false) String userId) {
        Map<String, Object> data = summaryService.getMonthlyConsumption(getUserId(userId), yearMonth);
        return JsonResponse.ok("월별 소비 금액 조회 성공", data);
    }

    @GetMapping("/categories")
    public ResponseEntity<Object> getMonthlyCategorySummary(@RequestParam("yearMonth") String yearMonth,
                                                            @RequestParam(value = "userId", required = false) String userId) {
        List<CategorySummaryDTO> categories = summaryService.getMonthlyCategorySummary(getUserId(userId), yearMonth);
        return JsonResponse.ok("월별 카테고리 소비비율 조회 성공",
                Map.of("yearMonth", yearMonth, "categories", categories));
    }

    @GetMapping("/overview")
    public ResponseEntity<Object> getOverviewConsumption(@RequestParam(value = "userId", required = false) String userId) {
        Map<String, Object> data = summaryService.getOverviewConsumption(getUserId(userId));
        return JsonResponse.ok("월 예상 소비 및 오늘의 소비 조회 성공", data);
    }

    @GetMapping("/today/categories")
    public ResponseEntity<Object> getTodayCategoryConsumption(@RequestParam(value = "userId", required = false) String userId) {
        Map<String, Object> data = summaryService.getTodayCategoryConsumption(getUserId(userId));
        return JsonResponse.ok("오늘의 소비 카테고리 비율 조회 성공", data);
    }

    @GetMapping("/categories/view")
    public String viewCategories(@RequestParam("yearMonth") String yearMonth,
                                 @RequestParam(value = "userId", required = false) String userId,
                                 Model model) {
        List<CategorySummaryDTO> categories = summaryService.getMonthlyCategorySummary(getUserId(userId), yearMonth);
        model.addAttribute("yearMonth", yearMonth);
        model.addAttribute("categories", categories);
        return "categories";
    }
}
