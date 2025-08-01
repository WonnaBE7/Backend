package com.wonnabe.asset.controller;

import com.wonnabe.asset.service.ConsumptionSummaryService;
import com.wonnabe.common.security.account.domain.CustomUser;
import com.wonnabe.common.util.JsonResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

    //메인페이지 - 이번 달 소비 현황
    @GetMapping("/main/overview")
    public ResponseEntity<Object> getMonthlyOverview(@AuthenticationPrincipal CustomUser customUser) {
        String userId = customUser.getUser().getUserId();
        Map<String, Object> data = summaryService.getMonthlyOverview(userId);
        return JsonResponse.ok("이번달 소비 현황 조회 성공", data);
    }

    //소비분석 페이지 - 월별 소비 요약 (이번 달, 지난달, ….)
    @GetMapping("/monthly")
    public ResponseEntity<Object> getMonthlyConsumption(@RequestParam("yearMonth") String yearMonth,
                                                        @AuthenticationPrincipal CustomUser customUser) {
        return yearMonth.matches("^\\d{4}-\\d{2}$")
                ? JsonResponse.ok("월별 소비 금액 조회 성공",
                summaryService.getMonthlyConsumption(customUser.getUser().getUserId(), yearMonth))
                : JsonResponse.error(HttpStatus.BAD_REQUEST, "형식이 잘못된 연월입니다. 예: 2025-08");
    }

    //소비분석 페이지 - 월별 카테고리 비율
    @GetMapping("/categories")
    public ResponseEntity<Object> getMonthlyCategorySummary(@RequestParam("yearMonth") String yearMonth,
                                                            @AuthenticationPrincipal CustomUser customUser) {
        return yearMonth.matches("^\\d{4}-\\d{2}$")
                ? JsonResponse.ok("월별 카테고리 소비비율 조회 성공",
                Map.of("yearMonth", yearMonth,
                        "categories", summaryService.getMonthlyCategorySummary(customUser.getUser().getUserId(), yearMonth)))
                : JsonResponse.error(HttpStatus.BAD_REQUEST, "형식이 잘못된 연월입니다. 예: 2025-08");
    }

    //소비분석 페이지 - 월 예상 소비 및 오늘의 소비
    @GetMapping("/overview")
    public ResponseEntity<Object> getOverviewConsumption(@AuthenticationPrincipal CustomUser customUser) {
        String userId = customUser.getUser().getUserId();
        Map<String, Object> data = summaryService.getOverviewConsumption(userId);
        return JsonResponse.ok("월 예상 소비 및 오늘의 소비 조회 성공", data);
    }

    //소비분석 페이지 - 오늘의 소비 카테고리 비율
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
        if (!yearMonth.matches("^\\d{4}-\\d{2}$")) {
            model.addAttribute("errorMessage", "형식이 잘못된 연월입니다. 예: 2025-08");
            return "error"; // 에러 페이지 템플릿이 있을 경우
        }

        String userId = customUser.getUser().getUserId();
        List<Map<String, Object>> categories = summaryService.getMonthlyCategorySummary(userId, yearMonth);
        model.addAttribute("yearMonth", yearMonth);
        model.addAttribute("categories", categories);
        return "categories";
    }
}
