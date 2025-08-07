package com.wonnabe.codef.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CodefTransactionResponse {
    private Result result;
    private TransactionListResponse data;
    private String connectedId;
}
