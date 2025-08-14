package com.wonnabe.codef.dto.invest.account;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class InvestAccountItem {

    @JsonProperty("resAccount")
    private String account;

    @JsonProperty("resAccountDisplay")
    private String accountDisplay;

    @JsonProperty("resAccountName")
    private String accountName;

    @JsonProperty("resAccountNickName")
    private String accountNickName;

    @JsonProperty("resPrincipal")
    private String principal;

    @JsonProperty("resPurchaseAmount")
    private String purchaseAmount;

    @JsonProperty("resLoanAmt")
    private String loanAmount;

    @JsonProperty("resValuationAmt")
    private String valuationAmount;

    @JsonProperty("resValuationPL")
    private String valuationPL;

    @JsonProperty("resWithdrawalAmt")
    private String withdrawalAmount;

    @JsonProperty("resDepositReceived")
    private String depositReceived;

    @JsonProperty("resDepositReceivedD1")
    private String depositReceivedD1;

    @JsonProperty("resDepositReceivedD2")
    private String depositReceivedD2;

    @JsonProperty("resDepositReceivedF")
    private String depositReceivedF;

    @JsonProperty("resEarningsRate")
    private String earningsRate;
}
