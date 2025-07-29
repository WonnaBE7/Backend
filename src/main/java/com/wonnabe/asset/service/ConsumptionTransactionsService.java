package com.wonnabe.asset.service;

import com.wonnabe.asset.dto.TransactionDTO;
import com.wonnabe.asset.mapper.ConsumptionTransactionsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConsumptionTransactionsService {

    @Autowired
    private ConsumptionTransactionsMapper transactionsMapper;

    /**
     * 월별 거래내역 조회
     */
    public List<TransactionDTO> getMonthlyTransactions(String userId, String yearMonth) {
        return transactionsMapper.getMonthlyTransactions(userId, yearMonth);
    }

    //카테고리 상세 거래 내역
    public List<TransactionDTO> getTransactionsByCategory(String userId, String category) {
        return transactionsMapper.getTransactionsByCategory(userId, category);
    }
}
