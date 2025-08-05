package com.wonnabe.codef.domain;

import lombok.Data;

@Data
public class UserTransactions {
    private String userId;                  // 사용자 UUID
    private String accountNumber;           // 계좌번호
    private String transactionDate;         // 거래일자 (yyyyMMdd)
    private String transactionTime;         // 거래시각 (hhmmss)
    private Long depositAmount;             // 입금금액
    private Long withdrawalAmount;          // 출금금액
//    private Long balanceAfter;              // 거래 후 잔액

    private String transactionType;         // 입금 or 출금
    private String assetCategory;

    private Long amount;
    private String description; // 기존 하나로 통합

    private String institutionCode;         // 기관 코드
//    private Date createdAt;                 // 수집 시각

    private String accountId;        // DB에 저장될 FK
}
