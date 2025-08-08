package com.wonnabe.codef.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wonnabe.codef.domain.UserTransactions;
import com.wonnabe.codef.mapper.AccountMapper;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.fasterxml.jackson.core.io.NumberInput.parseBigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionListResponse {

    @JsonProperty("resAccountBalance")
    private String accountBalance;

    @JsonProperty("resWithdrawalAmt")
    private String totalWithdrawalAmount;

    @JsonProperty("resAccountDisplay")
    private String displayAccountNumber;

    @JsonProperty("resAccount")
    private String accountNumber;

    @JsonProperty("resAccountName")
    private String accountName;

    @JsonProperty("resAccountNickName")
    private String accountNickName;

    @JsonProperty("resAccountHolder")
    private String accountHolder;

    @JsonProperty("resAccountStartDate")
    private String accountStartDate;

    @JsonProperty("resManagementBranch")
    private String managementBranch;

    @JsonProperty("resAccountStatus")
    private String accountStatus;

    @JsonProperty("resLastTranDate")
    private String lastTransactionDate;

    @JsonProperty("resLoanEndDate")
    private String loanEndDate;

    @JsonProperty("resLoanLimitAmt")
    private String loanLimitAmount;

    @JsonProperty("resInterestRate")
    private String interestRate;

    @JsonProperty("commStartDate")
    private String startDate;

    @JsonProperty("commEndDate")
    private String endDate;

    @JsonProperty("resTrHistoryList")
    private List<TransactionDetail> historyList;

    public List<UserTransactions> toUserTransactions(String userId, String institutionCode, AccountMapper accountMapper) {
        List<UserTransactions> result = new ArrayList<>();
        if (historyList == null) return result;

        for (TransactionDetail detail : historyList) {
            UserTransactions tx = new UserTransactions();

            tx.setUserId(userId);
            tx.setTransactionDate(detail.getTransactionDate());  // yyyy-MM-dd 형식 가정
            tx.setTransactionTime(detail.getTransactionTime());  // HH:mm:ss 형식 가정
            tx.setDescription1(detail.getDescription1());
            tx.setDescription2(detail.getDescription2());
            tx.setDescription3(detail.getDescription3());
            tx.setDescription4(detail.getDescription4());
//            tx.setAssetCategory(assetCategory);                  // '예금','적금','투자','입출금','보험','기타' // '입출금','투자','연금','기타'

            Long in = parseLong(detail.getDepositAmount());
            Long out = parseLong(detail.getWithdrawalAmount());

            if (in != null && in != 0) {
                tx.setAmount(in);
                tx.setTransactionType("입금");
            } else if (out != null && out != 0) {
                tx.setAmount(-out);
                tx.setTransactionType("출금");
            } else {
                // null이거나 0이면 무시
                continue;
            }

            // ✅ accountId 세팅
            Long accountId = accountMapper.findAccountIdByUserIdAndAccountNumber(userId, this.accountNumber);
            if (accountId == null) {
//                log.warn("❗ account_id not found for userId={}, accountNumber={}", userId, accountNumber);
                continue;
            }
            tx.setAccountId(String.valueOf(accountId));

            // ✅ assetCategory 조회 및 설정
            String assetCategory = accountMapper.findCategoryByAccountId(accountId);
            if (assetCategory != null && !assetCategory.isBlank()) {
                tx.setAssetCategory(assetCategory);  // 예: "입출금", "예금", "투자", "연금", "기타" + 추가처리(이 외라면 User_Insurance, User_Savings에서 찾도록)
            }
            tx.setInstitutionCode(institutionCode);
            result.add(tx);
        }
        return result;
    }

    private Long parseLong(String value) {
        try {
            return value != null && !value.isBlank() ? Long.parseLong(value.replaceAll(",", "")) : null;
        } catch (Exception e) {
            return null;
        }
    }
}
