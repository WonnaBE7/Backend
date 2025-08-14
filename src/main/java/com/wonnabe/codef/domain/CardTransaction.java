package com.wonnabe.codef.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardTransaction {

    private Long id;                    // PK
    private String userId;               // char(36) - UUID 문자열
    private Long cardId;                 // FK -> User_card.id

    private LocalDate transactionDate;   // 거래일자
    private LocalTime transactionTime;   // 거래시각 (nullable)

    private String cardNumber;           // 카드번호
    private String cardName;             // 카드명

    private String merchantName;         // 가맹점명
    private MerchantCategory merchantCategory; // 가맹점 카테고리(enum)
    private String merchantStoreType; // 가맹점 업종

    private String merchantCorpNo;       // 가맹점 사업자등록번호
    private String merchantStoreNo;      // 가맹점 번호

    private BigDecimal amount;           // 거래금액

    private LocalDateTime createdAt;     // 생성일시 (기본값 CURRENT_TIMESTAMP)

    public enum MerchantCategory {
        food,
        transport,
        shopping,
        culture,
        other
    }
}
