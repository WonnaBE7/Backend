package com.wonnabe.codef.dto.bank.transaction;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BankAccountTransactionMeta {
    private String code;
    private String message;
    private String extraMessage;
    private String transactionId;
}
