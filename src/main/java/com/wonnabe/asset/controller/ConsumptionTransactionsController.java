package com.wonnabe.asset.controller;

import com.wonnabe.asset.dto.TransactionDTO;
import com.wonnabe.asset.service.ConsumptionTransactionsService;
import com.wonnabe.common.security.account.domain.CustomUser;
import com.wonnabe.common.util.JsonResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assets/consumption")
public class ConsumptionTransactionsController {

    @Autowired
    private ConsumptionTransactionsService transactionsService;

    @GetMapping("/transactions")
    public ResponseEntity<Object> getMonthlyTransactions(@RequestParam("yearMonth") String yearMonth,
                                                         @AuthenticationPrincipal CustomUser customUser) {
        // 유효성 검사 추가
        if (!yearMonth.matches("^\\d{4}-\\d{2}$")) {
            throw new IllegalArgumentException("형식이 잘못된 연월입니다. 예: 2025-08");
        }

        String userId = customUser.getUser().getUserId();
        List<TransactionDTO> transactions = transactionsService.getMonthlyTransactions(userId, yearMonth);
        return JsonResponse.ok("월 별 거래 내역 조회 성공",
                Map.of("yearMonth", yearMonth, "transactions", transactions));
    }


    @GetMapping("/transactions/category")
    public ResponseEntity<Object> getTransactionsByCategory(@RequestParam("category") String category,
                                                            @AuthenticationPrincipal CustomUser customUser) {
        //소비 카테고리 유효성 검사
        List<String> VALID_CATEGORIES = List.of(
                "food", "transport", "shopping", "financial", "culture", "life", "medical", "communication", "housing", "education", "etc"
        );

        if (!VALID_CATEGORIES.contains(category)) {
            throw new IllegalArgumentException("유효하지 않은 소비 카테고리입니다: " + category);
        }

        String userId = customUser.getUser().getUserId();
        List<TransactionDTO> transactions = transactionsService.getTransactionsByCategory(userId, category);

        return JsonResponse.ok("카테고리별 상세 거래 내역 조회 성공",
                Map.of("consumptionCategory", category, "transactions", transactions));
    }


    @GetMapping("/transactions/today")
    public ResponseEntity<Object> getTodayTransactions(@AuthenticationPrincipal CustomUser customUser) {
        String userId = customUser.getUser().getUserId();
        String today = LocalDate.now().toString();
        List<TransactionDTO> transactions = transactionsService.getTodayTransactions(userId, today);
        return JsonResponse.ok("오늘의 거래 내역 조회 성공",
                Map.of("date", today, "transactions", transactions));
    }

    @GetMapping("/transactions/today/category")
    public ResponseEntity<Object> getTodayTransactionsByCategory(@RequestParam("category") String category,
                                                                 @AuthenticationPrincipal CustomUser customUser) {
        List<String> VALID_CATEGORIES = List.of(
                "food", "transport", "shopping", "financial", "culture", "life", "medical", "communication", "housing", "education", "etc"
        );

        if (!VALID_CATEGORIES.contains(category)) {
            throw new IllegalArgumentException("유효하지 않은 소비 카테고리입니다: " + category);
        }

        String userId = customUser.getUser().getUserId();
        String today = LocalDate.now().toString();
        List<TransactionDTO> transactions = transactionsService.getTodayTransactionsByCategory(userId, today, category);
        return JsonResponse.ok("오늘 소비 카테고리별 상세 거래 내역 조회 성공",
                Map.of("consumptionCategory", category, "transactions", transactions));
    }
}
