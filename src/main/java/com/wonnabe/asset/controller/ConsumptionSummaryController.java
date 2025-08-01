package com.wonnabe.asset.controller;

import com.wonnabe.asset.service.ConsumptionSummaryService;
import com.wonnabe.common.security.account.domain.CustomUser;
import com.wonnabe.common.util.JsonResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assets/consumption")
public class ConsumptionSummaryController {

    @Autowired
    private ConsumptionSummaryService summaryService;

    @GetMapping("/main/overview")
    public ResponseEntity<Object> getMonthlyOverview(@AuthenticationPrincipal CustomUser customUser) {
        String userId = customUser.getUser().getUserId();
        Map<String, Object> data = summaryService.getMonthlyOverview(userId);
        return JsonResponse.ok("이번달 소비 현황 조회 성공", data);
    }

    @GetMapping("/monthly")
    public ResponseEntity<Object> getMonthlyConsumption(@RequestParam("yearMonth") String yearMonth,
                                                        @AuthenticationPrincipal CustomUser customUser) {
        String userId = customUser.getUser().getUserId();
        Map<String, Object> data = summaryService.getMonthlyConsumption(userId, yearMonth);
        return JsonResponse.ok("월별 소비 금액 조회 성공", data);
    }

    @GetMapping("/categories")
    public ResponseEntity<Object> getMonthlyCategorySummary(@RequestParam("yearMonth") String yearMonth,
                                                            @AuthenticationPrincipal CustomUser customUser) {
        String userId = customUser.getUser().getUserId();
        List<Map<String, Object>> categories = summaryService.getMonthlyCategorySummary(userId, yearMonth);
        return JsonResponse.ok("월별 카테고리 소비비율 조회 성공",
                Map.of("yearMonth", yearMonth, "categories", categories));
    }

    @GetMapping("/overview")
    public ResponseEntity<Object> getOverviewConsumption(@AuthenticationPrincipal CustomUser customUser) {
        String userId = customUser.getUser().getUserId();
        Map<String, Object> data = summaryService.getOverviewConsumption(userId);
        return JsonResponse.ok("월 예상 소비 및 오늘의 소비 조회 성공", data);
    }

    @GetMapping("/today/categories")
    public ResponseEntity<Object> getTodayCategoryConsumption(@AuthenticationPrincipal CustomUser customUser) {
        String userId = customUser.getUser().getUserId();
        Map<String, Object> data = summaryService.getTodayCategoryConsumption(userId);
        return JsonResponse.ok("오늘의 소비 카테고리 비율 조회 성공", data);
    }

    @GetMapping("/categories/view")
    public String viewCategories(@RequestParam("yearMonth") String yearMonth,
                                 @AuthenticationPrincipal CustomUser customUser,
                                 Model model) {
        String userId = customUser.getUser().getUserId();
        List<Map<String, Object>> categories = summaryService.getMonthlyCategorySummary(userId, yearMonth);
        model.addAttribute("yearMonth", yearMonth);
        model.addAttribute("categories", categories);
        return "categories";
    }
}
