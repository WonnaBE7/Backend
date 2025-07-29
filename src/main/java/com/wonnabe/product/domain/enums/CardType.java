package com.wonnabe.product.domain.enums;

import lombok.Getter;

// 카드 타입 설정
@Getter
public enum CardType {
    CREDIT("신용"), // 신용 카드용
    DEBIT("체크"); // 체크 카드용

    // 라벨 값 반환 - (신용 or 체크)
    private final String label; // 신용 체크 중 저장되는 값

    // 생성자
    CardType(String label) {
        this.label = label;
    }

    // 타입 값 반환 - (CREDIT or DEBIT)
    public static CardType fromLabel(String label) {
        for (CardType type : values()) {
            if (type.label.equals(label)) {
                return type;
            }
        }
        return null;
    }
}
