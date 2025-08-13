package com.wonnabe.asset.controller;

import com.wonnabe.asset.domain.ConsumptionCategory;
import com.wonnabe.asset.dto.TransactionDTO;
import com.wonnabe.asset.service.ConsumptionTransactionsService;
import com.wonnabe.common.security.account.domain.CustomUser;
import com.wonnabe.common.util.JsonResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/assets/consumption")
public class ConsumptionTransactionsController {

    @Autowired
    private ConsumptionTransactionsService transactionsService;

    //소비분석 페이지 - 월별 거래내역
    @GetMapping("/transactions")
    public ResponseEntity<Object> getMonthlyTransactions(@RequestParam("yearMonth") String yearMonth,
                                                         @AuthenticationPrincipal CustomUser customUser) {
        return yearMonth.matches("^\\d{4}-\\d{2}$")
                ? JsonResponse.ok("월 별 거래 내역 조회 성공",
                Map.of("yearMonth", yearMonth,
                        "transactions", transactionsService.getMonthlyTransactions(customUser.getUser().getUserId(), yearMonth)))
                : JsonResponse.error(HttpStatus.BAD_REQUEST, "형식이 잘못된 연월입니다. 예: 2025-08");
    }

    // 소비분석 상세페이지 - 카테고리별 상세 거래 내역
    @GetMapping("/transactions/category")
    public ResponseEntity<Object> getTransactionsByCategory(
            @RequestParam("category") String category,
            @RequestParam("yearMonth") String yearMonth,
            @AuthenticationPrincipal CustomUser customUser) {

        try {
            ConsumptionCategory cat = ConsumptionCategory.fromValue(category);

            return (!yearMonth.matches("^\\d{4}-(0[1-9]|1[0-2])$"))
                    ? JsonResponse.error(HttpStatus.BAD_REQUEST, "yearMonth는 YYYY-MM 형식이어야 합니다.")
                    : JsonResponse.ok(
                    "월별 카테고리별 상세 거래 내역 조회 성공",
                    Map.of(
                            "consumptionCategory", cat.getValue(),
                            "transactions", transactionsService.getTransactionsByCategory(
                                    customUser.getUser().getUserId(), cat.getValue(), yearMonth)
                    )
            );
        } catch (IllegalArgumentException e) {
            return JsonResponse.error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }



    //소비분석 페이지 - 오늘의 거래내역
    @GetMapping("/transactions/today")
    public ResponseEntity<Object> getTodayTransactions(@AuthenticationPrincipal CustomUser customUser) {
        String userId = customUser.getUser().getUserId();
        String today = LocalDate.now().toString();
        List<TransactionDTO> transactions = transactionsService.getTodayTransactions(userId, today);
        return JsonResponse.ok("오늘의 거래 내역 조회 성공",
                Map.of("date", today, "transactions", transactions));
    }

    // 소비분석 상세페이지 - 오늘의 카테고리별 상세 거래 내역
    @GetMapping("/transactions/today/category")
    public ResponseEntity<Object> getTodayTransactionsByCategory(
            @RequestParam("category") String category,
            @AuthenticationPrincipal CustomUser customUser) {

        try {
            ConsumptionCategory cat = ConsumptionCategory.fromValue(category);
            String userId = customUser.getUser().getUserId();
            String today = LocalDate.now().toString(); // ex) 2025-08-12

            return JsonResponse.ok("오늘 소비 카테고리별 상세 거래 내역 조회 성공",
                    Map.of(
                            "date", today,
                            "consumptionCategory", cat.getValue(),
                            "transactions", transactionsService.getTodayTransactionsByCategory(userId, today, cat)
                    ));
        } catch (IllegalArgumentException e) {
            return JsonResponse.error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
