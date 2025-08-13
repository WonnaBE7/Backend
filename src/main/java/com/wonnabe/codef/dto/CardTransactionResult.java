package com.wonnabe.codef.dto;

import lombok.Data;

@Data
public class CardTransactionResult {
    private String code;
    private String extraMessage;
    private String message;
    private String transactionId;
}
