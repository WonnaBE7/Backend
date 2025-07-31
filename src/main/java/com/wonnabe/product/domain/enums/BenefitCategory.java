package com.wonnabe.product.domain.enums;

// 혜택에 해당하는 카테고리 설정
public enum BenefitCategory {
    Food("식비"), // 식비용
    Transport("교통"), // 교통용
    Shopping("쇼핑"), // 쇼핑용
    Financial("금융"), // 금융용
    Other("기타"); // 그 외 나머지용

    private final String label; // 위의 5개 중 저장되는 값

    // 생성자
    private BenefitCategory(String label) {
        this.label = label;
    }

    // 라벨 값 반환 - (위의 5가지 혜택 영역)
    public String getLabel() {
        return label;
    }

    // 타입 값 반환 - (Food, Transport 등...)
    public static BenefitCategory fromLabel(String label) {
        for (BenefitCategory type : values()) {
            if (type.label.equals(label)) {
                return type;
            }
        }
        return null;
    }
}
