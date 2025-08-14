package com.wonnabe.asset.domain;

public enum ConsumptionCategory {
    FOOD("food"),
    TRANSPORT("transport"),
    SHOPPING("shopping"),
    CULTURE("culture"),
    OTHER("other");

    private final String value;

    ConsumptionCategory(String value) { this.value = value; }
    public String getValue() { return value; }

    public static ConsumptionCategory fromValue(String value) {
        for (ConsumptionCategory c : values()) {
            if (c.value.equalsIgnoreCase(value)) return c;
        }
        throw new IllegalArgumentException("유효하지 않은 소비 카테고리입니다: " + value);
    }
}
