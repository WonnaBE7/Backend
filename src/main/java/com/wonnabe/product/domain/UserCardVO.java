package com.wonnabe.product.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCardVO {
    private long id; // 고유 ID
    private UUID userId; // 사용자 ID
    private long productId; // 카드 ID
    private double monthlyUsage; // 월 사용 실적
    private Date issueDate; // 발급일
    private Date expiryDate; // 만료일
    private long performanceCondition; // 실적
    private String cardNumber; // 카드 번호
}
