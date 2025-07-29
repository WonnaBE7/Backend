package com.wonnabe.asset.controller;

import com.wonnabe.asset.dto.TransactionDTO;
import com.wonnabe.asset.service.ConsumptionTransactionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assets/consumption")
public class ConsumptionTransactionsController {

    @Autowired
    private ConsumptionTransactionsService transactionsService;

    /**
     * 월별 거래 내역 조회
     */
    @GetMapping("/transactions")
    public Map<String, Object> getMonthlyTransactions(
            @RequestParam("yearMonth") String yearMonth,
            @RequestParam(value = "userId", required = false) String userId) {

        if (userId == null) {
            userId = "111e2222-aaaa-bbbb-cccc-123456789000"; // 테스트용
        }

        List<TransactionDTO> transactions = transactionsService.getMonthlyTransactions(userId, yearMonth);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("yearMonth", yearMonth);
        data.put("transactions", transactions);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("code", 200);
        response.put("message", "월 별 거래 내역 조회 성공");
        response.put("data", data);

        return response;
    }
    /**
     * 카테고리별 상세 거래 내역 조회
     */
    @GetMapping("/transactions/category")
    public Map<String, Object> getTransactionsByCategory(
            @RequestParam("category") String category,
            @RequestParam(value = "userId", required = false) String userId) {

        if (userId == null) {
            userId = "111e2222-aaaa-bbbb-cccc-123456789000";
        }

        List<TransactionDTO> transactions = transactionsService.getTransactionsByCategory(userId, category);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("consumptionCategory", category);
        data.put("transactions", transactions);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("code", 200);
        response.put("message", "카테고리별 상세 거래 내역 조회 성공");
        response.put("data", data);

        return response;
    }
}
