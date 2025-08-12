package com.wonnabe.codef.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvestAccountListResult {
    private String code;
    private String extraMessage;
    private String message;
    private String transactionId;
}
