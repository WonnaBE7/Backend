package com.wonnabe.codef.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Result {
    private String code;
    private String message;
    private String extraMessage;
    private String transactionId;
}
