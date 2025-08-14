package com.wonnabe.codef.dto.bank.savings;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wonnabe.codef.domain.UserTransaction;
import com.wonnabe.codef.mapper.AssetSavingsMapper;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class BankSavingsTransactionListResponse {

    @JsonProperty("data")
    private BankSavingsListPayload data;

    public List<UserTransaction> toUserTransactions(String userId, String institutionCode, AssetSavingsMapper savingMapper) {
        List<UserTransaction> transactions = new ArrayList<>();
        if (data == null || data.getResTrHistoryList() == null) return transactions;

        for (Map<String, Object> detail : data.getResTrHistoryList()) {
            Long deposit = parseLong(detail.get("resAccountIn"));

            if ((deposit == null || deposit == 0)) {
                continue;
            }

            UserTransaction tx = new UserTransaction();

            tx.setUserId(userId);
            tx.setInstitutionCode(institutionCode);
            tx.setAccountNumber(data.getResAccount());
            tx.setTransactionDate((String) detail.get("resAccountTrDate"));
            tx.setTransactionTime(null);
            tx.setDepositAmount(deposit);
            tx.setWithdrawalAmount(0L);
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
