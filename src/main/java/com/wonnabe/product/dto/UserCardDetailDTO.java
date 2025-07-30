package com.wonnabe.product.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.Period;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCardDetailDTO {
    private String cardId; // 카드 ID
    private String cardName; // 카드 이름
    private String cardCompany; // 카드 제조사
    @JsonFormat(pattern = "yyyy-MM")
    private LocalDate startDate; // 카드 발급일
    @JsonFormat(pattern = "yyyy-MM")
    private LocalDate expiryDate; // 카드 만료일
    private String term; // 카드 유지 기간
    private double currentAmount; // 올해 카드 사용량
    private int performanceRate; // 카드 활용도
    List<MonthlyConsumptionDTO> monthlyConsumptions; // 월별 사용량

    // UsercardDto와 term, performanceRate를 받아 해당 클래스의 인스턴스를 생성하는 함수
    public static UserCardDetailDTO custom(UserCardDTO userCardDTO, int term, int performanceRate) {
        return userCardDTO == null ? null : UserCardDetailDTO.builder()
                .cardId(String.valueOf(userCardDTO.getId()))
                .cardName(userCardDTO.getCardName())
                .cardCompany(userCardDTO.getCardCompany())
                .startDate(userCardDTO.getIssueDate())
                .expiryDate(userCardDTO.getExpiryDate())
                .term(term+"개월")
                .currentAmount(userCardDTO.getMonthlyUsage())
                .performanceRate(performanceRate)
                .monthlyConsumptions(userCardDTO.getConsumptions())
                .build();
    }
}
