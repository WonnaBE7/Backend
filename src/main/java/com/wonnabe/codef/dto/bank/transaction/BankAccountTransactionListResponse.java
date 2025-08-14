package com.wonnabe.codef.dto.bank.transaction;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class BankAccountTransactionListResponse {
    private BankAccountTransactionMeta result;
    private BankAccountTransactionPayload data;
    private String connectedId;
}
