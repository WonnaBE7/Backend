package com.wonnabe.codef.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wonnabe.codef.domain.UserAccount;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountListResponse {

    @JsonProperty("data")
    private CodefData data;

    public List<UserAccount> toUserAccountsFromDeposit(String userId, String institutionCode) {
        return parseAccounts(data.getResDepositTrust(), userId, institutionCode, "입출금");
    }

    public List<UserAccount> toUserAccountsFromInsurance(String userId, String institutionCode) {
        return parseAccounts(data.getResInsurance(), userId, institutionCode, "보험");
    }

    private List<UserAccount> parseAccounts(List<Map<String, Object>> list, String userId, String institutionCode, String category) {
        List<UserAccount> accounts = new ArrayList<>();
        if (list == null) return accounts;

        for (Map<String, Object> item : list) {
            UserAccount account = new UserAccount();
            account.setUserId(userId);
            account.setInstitutionCode(institutionCode);

            account.setAccountNumber((String) item.get("resAccount"));
            account.setAccountBalance(parseDouble(item.get("resAccountBalance")));
            account.setAccountDeposit((String) item.get("resAccountDeposit"));
            account.setAccountNickname((String) item.get("resAccountNickName"));
            account.setCurrency((String) item.get("resAccountCurrency"));
            account.setAccountStartDate((String) item.get("resAccountStartDate"));
            account.setAccountEndDate((String) item.get("resAccountEndDate"));
            account.setAccountName((String) item.get("resAccountName"));
            account.setAccountDisplay((String) item.get("resAccountDisplay"));
//            account.setCategory(category);

            accounts.add(account);
        }

        return accounts;
    }

    private Double parseDouble(Object value) {
        try {
            return value != null ? Double.parseDouble(value.toString()) : null;
        } catch (Exception e) {
            return null;
        }
    }

    public List<UserAccount> toUserAccounts(String userId, String institutionCode) {
        List<UserAccount> result = new ArrayList<>();

        if (data == null) return result;

        result.addAll(parseAccounts(data.getResDepositTrust(), userId, institutionCode, "deposit"));
        result.addAll(parseAccounts(data.getResForeignCurrency(), userId, institutionCode, "foreign"));
        result.addAll(parseAccounts(data.getResFund(), userId, institutionCode, "fund"));
        result.addAll(parseAccounts(data.getResLoan(), userId, institutionCode, "loan"));
        result.addAll(parseAccounts(data.getResInsurance(), userId, institutionCode, "insurance"));

        return result;
    }
}
