package com.wonnabe.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCardDTO {
    private Long id; // 고유 ID
    private String userId; // 사용자 ID
    private String cardCompany;
    private String cardName;
    private Long productId; // 카드 ID
    private Double monthlyUsage; // 월 사용 실적
    private LocalDate issueDate; // 발급일
    private LocalDate expiryDate; // 만료일
    private Long performanceCondition; // 실적
    private String cardNumber; // 카드 번호
    List<MonthlyConsumptionDTO> consumptions; // 이전 소비 내역
}
