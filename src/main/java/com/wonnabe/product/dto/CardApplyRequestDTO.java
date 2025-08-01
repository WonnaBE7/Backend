package com.wonnabe.product.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardApplyRequestDTO
{
    String productType; // 상품의 종류
    String cardId; // 카드의 아이디
    String cardType; // 카드의 종류
    String linkedAccount; // 연결계좌
}
