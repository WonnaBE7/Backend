package com.wonnabe.codef.domain;

import lombok.Data;

@Data
public class UserAccount {
    private String userId;
    private String institutionCode;

    private String accountNumber;
    private Double accountBalance;
    private String accountDeposit;
    private String accountNickname;
    private String currency;

    private String accountStartDate;
    private String accountEndDate;

    private String accountName;
    private String accountDisplay;
    private String category;
}
