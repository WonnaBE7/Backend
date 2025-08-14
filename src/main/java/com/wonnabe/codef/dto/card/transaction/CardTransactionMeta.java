package com.wonnabe.codef.dto.card.transaction;

import lombok.Data;

@Data
public class CardTransactionMeta {
    private String code;
    private String extraMessage;
    private String message;
    private String transactionId;
}
