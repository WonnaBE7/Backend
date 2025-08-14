package com.wonnabe.codef.dto.bank.account;

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
public class BankAccountListResponse {

    @JsonProperty("data")
    private BankAccountListData data;

    public List<UserAccount> toUserAccountsFromDeposit(String userId, String institutionCode) {
        return parseAccounts(data.getResDepositTrust(), userId, institutionCode);
    }

    private List<UserAccount> parseAccounts(List<Map<String, Object>> list, String userId, String institutionCode) {
        List<UserAccount> accounts = new ArrayList<>();
        if (list == null) return accounts;

        for (Map<String, Object> item : list) {
            UserAccount account = new UserAccount();
            account.setUserId(userId);
            account.setInstitutionCode(institutionCode);

            account.setAccountNumber((String) item.get("resAccount"));
            account.setAccountName((String) item.get("resAccountName"));
            account.setAccountDeposit((String) item.get("resAccountDeposit"));
            account.setAccountBalance(parseDouble(item.get("resAccountBalance")));
            account.setAccountStartDate((String) item.get("resAccountStartDate"));
            account.setAccountEndDate((String) item.get("resAccountEndDate"));

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
}
