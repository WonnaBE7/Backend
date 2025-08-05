package com.wonnabe.codef.domain;

import lombok.Data;

import java.util.Date;

@Data
public class UserCard {
    private String userId;           // 사용자 UUID
    private String cardName;         // resCardName
    private String cardNumber;       // resCardNo
    private String cardType;         // resCardType
    private String userName;         // resUserNm
    private String sleepYn;          // resSleepYN
    private String trafficYn;        // resTrafficYN
    private Date validPeriod;        // resValidPeriod (yyyyMMdd → Date)
    private Date issueDate;          // resIssueDate (yyyyMMdd → Date)
    private String imageLink;        // resImageLink
    private String cardState;        // resState

    private Long accountId;          // User_Accounts 테이블 FK (옵션)
    private Long productId;          // Card_product 테이블 FK (추론 필요)
}
