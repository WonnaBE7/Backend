package com.wonnabe.asset.controller;

import com.wonnabe.asset.dto.TransactionDTO;
import com.wonnabe.asset.service.ConsumptionTransactionsService;
import com.wonnabe.common.util.JsonResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assets/consumption")
public class ConsumptionTransactionsController {

    @Autowired
    private ConsumptionTransactionsService transactionsService;

    /** userId 기본값 처리 */
    private String getUserId(String userId) {
        return (userId != null) ? userId : "111e2222-aaaa-bbbb-cccc-123456789000";
    }

    @GetMapping("/transactions")
    public ResponseEntity<Object> getMonthlyTransactions(@RequestParam("yearMonth") String yearMonth,
                                                         @RequestParam(value = "userId", required = false) String userId) {
        List<TransactionDTO> transactions = transactionsService.getMonthlyTransactions(getUserId(userId), yearMonth);
        return JsonResponse.ok("월 별 거래 내역 조회 성공",
                Map.of("yearMonth", yearMonth, "transactions", transactions));
    }

    @GetMapping("/transactions/category")
    public ResponseEntity<Object> getTransactionsByCategory(@RequestParam("category") String category,
                                                            @RequestParam(value = "userId", required = false) String userId) {
        List<TransactionDTO> transactions = transactionsService.getTransactionsByCategory(getUserId(userId), category);
        return JsonResponse.ok("카테고리별 상세 거래 내역 조회 성공",
                Map.of("consumptionCategory", category, "transactions", transactions));
    }

    @GetMapping("/transactions/today")
    public ResponseEntity<Object> getTodayTransactions(@RequestParam(value = "userId", required = false) String userId) {
        String today = LocalDate.now().toString();
        List<TransactionDTO> transactions = transactionsService.getTodayTransactions(getUserId(userId), today);
        return JsonResponse.ok("오늘의 거래 내역 조회 성공",
                Map.of("date", today, "transactions", transactions));
    }

    @GetMapping("/transactions/today/category")
    public ResponseEntity<Object> getTodayTransactionsByCategory(@RequestParam("category") String category,
                                                                 @RequestParam(value = "userId", required = false) String userId) {
        String today = LocalDate.now().toString();
        List<TransactionDTO> transactions = transactionsService.getTodayTransactionsByCategory(getUserId(userId), today, category);
        return JsonResponse.ok("오늘 소비 카테고리별 상세 거래 내역 조회 성공",
                Map.of("consumptionCategory", category, "transactions", transactions));
    }
}
