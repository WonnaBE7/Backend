package com.wonnabe.codef.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wonnabe.codef.domain.UserAccount;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Double.parseDouble;

// com.wonnabe.codef.dto.AccountListWrapper
@Getter @Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class InvestAccountListWrapper {
    private InvestAccountListResult result;

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<InvestAccountListResponse> data;
    private String connectedId;

    // 기존: 은행용 메서드가 있다면 유지 (예: toUserAccountsFromDeposit)

    // 신규: 증권 보유계좌 → UserAccount 변환
    public List<UserAccount> toUserAccountsFromSecurities(String userId, String institutionCode) {
        List<UserAccount> list = new ArrayList<>();
        if (data == null) return list;

        for (InvestAccountListResponse src : data) {
            UserAccount ua = new UserAccount();
            ua.setUserId(userId);
            ua.setInstitutionCode(institutionCode);

            // 계좌 식별
            ua.setAccountNumber(src.getAccount());         // "711796978814"
            ua.setAccountDisplay(src.getAccountDisplay()); // "7117969788-14"
            ua.setAccountName(src.getAccountName());       // "개인종합자산관리(중개형)..." 등

            // 증권은 잔액 대신 평가금액을 주는 경우가 많음
            Double valuation = parseDouble(src.getValuationAmount());
            if (valuation != null) {
                ua.setAccountBalance(valuation);
            }

            // 손익, 매수원가 등도 원하면 보조 컬럼으로 저장 가능
            // ua.setLastMonthBalance(...), ua.setNote(...)

            // 증권은 예금코드(11/12/14)로 분류하지 말고 바로 카테고리 고정
            ua.setCategory("투자");

            // 은행명은 기존 로직에서 set 예정이므로 여기선 생략해도 됨
            // ua.setBankName(...);

            // deposit code 없음(은행 아님)
            ua.setAccountDeposit(null);

            list.add(ua);
        }
        return list;
    }

    private Long parseLong(String v) {
        try { return (v == null || v.isBlank()) ? null : Long.parseLong(v.replaceAll(",", "")); }
        catch (Exception e) { return null; }
    }
}
