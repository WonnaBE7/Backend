package com.wonnabe.codef.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountTransactionListWrapper {
    private AccountTransactionListWrapperResult result;
    private AccountTransactionListResponse data;
    private String connectedId;
}
