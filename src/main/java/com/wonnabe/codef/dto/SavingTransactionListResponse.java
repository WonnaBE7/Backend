package com.wonnabe.codef.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wonnabe.codef.domain.UserTransactions;
import com.wonnabe.codef.mapper.AccountMapper;
import com.wonnabe.codef.mapper.CodefSavingsMapper;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SavingTransactionListResponse {

    @JsonProperty("data")
    private SavingListResponse data;

    public List<UserTransactions> toUserTransactions(String userId, String institutionCode, AccountMapper accountMapper, CodefSavingsMapper savingMapper) {
        List<UserTransactions> transactions = new ArrayList<>();
        if (data == null || data.getResTrHistoryList() == null) return transactions;

        for (Map<String, Object> detail : data.getResTrHistoryList()) {
            Long deposit = parseLong(detail.get("resAccountIn"));
//            Long balance = parseLong(detail.get("resAfterTranBalance"));

            if ((deposit == null || deposit == 0)) {
                // 입금 금액이 없으면 무시
                continue;
            }

            UserTransactions tx = new UserTransactions();

            tx.setUserId(userId);
            tx.setInstitutionCode(institutionCode);
            tx.setAccountNumber(data.getResAccount());
            tx.setTransactionDate((String) detail.get("resAccountTrDate"));
            tx.setTransactionTime(null); // 적금 거래는 시간 없음. 고정값 또는 null
            tx.setDepositAmount(deposit);
            tx.setWithdrawalAmount(0L); // 적금은 출금 없음
            tx.setTransactionType("입금");
            tx.setAmount(deposit);
            tx.setDescription1((String) detail.get("resAccountDesc1"));
            tx.setDescription2((String) detail.get("resAccountDesc2"));
            tx.setDescription3((String) detail.get("resAccountDesc3"));
            tx.setDescription4((String) detail.get("resAccountDesc4"));

            Long accountId = savingMapper.findSavingIdByUserIdAndProductId(data.getResAccount());
            if (accountId == null && "66091007246752".equals(data.getResAccount())) {
                accountId = 9999L;
                tx.setAssetCategory("연금");
            } else if (accountId == null) {
                continue;
            }

            tx.setAccountId(String.valueOf(accountId));
            tx.setAssetCategory("예적금");
//            Integer productId = savingMapper.findProductIdByAccountId(accountId);
//            if (productId != null) {
//                if (productId >= 1300 && productId < 1400) {
//                    tx.setAssetCategory("적금");
//                } else if (productId >= 1500 && productId < 1600) {
//                    tx.setAssetCategory("예금");
//                } else {
//                    tx.setAssetCategory("기타");
//                }
//            }
            transactions.add(tx);
        }
        return transactions;
    }

    private Long parseLong(Object value) {
        try {
            return (value != null && !value.toString().isBlank())
                    ? Long.parseLong(value.toString())
                    : null;
        } catch (Exception e) {
            return null;
        }
    }
}
