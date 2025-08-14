package com.wonnabe.codef.dto.bank.account;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class BankAccountListData {

    @JsonProperty("resDepositTrust")
    private List<Map<String, Object>> resDepositTrust;

    @JsonProperty("resForeignCurrency")
    private List<Map<String, Object>> resForeignCurrency;

    @JsonProperty("resFund")
    private List<Map<String, Object>> resFund;

    @JsonProperty("resLoan")
    private List<Map<String, Object>> resLoan;

    @JsonProperty("resInsurance")
    private List<Map<String, Object>> resInsurance;
}
