package com.wonnabe.goal.domain;

import java.util.Arrays;

public enum ProductType {
    SAVINGS(1000L, 1500L),
    DEPOSIT(1500L, 2000L);

    private final long startId;
    private final long endId;

    ProductType(long startId, long endId) {
        this.startId = startId;
        this.endId = endId;
    }

    public static ProductType of(Long productId) {
        return Arrays.stream(values())
                .filter(type -> productId >= type.startId && productId < type.endId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown product type for ID: " + productId));
    }
}
