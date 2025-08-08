package com.wonnabe.product.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class InsuranceProductDetailResponseDTO {
    private ProductInfo productInfo;
    private List<ComparisonChart> comparisonChart;
    private MaturityInfo maturityInfo;

    @Data
    @Builder
    public static class ProductInfo {
        private String productId;
        private String productName;
        private String providerName;

        private int matchScore;
        private String coverageType;
        private String coverageLimit;
        private String deductible;
        private String averagePremium;

        private boolean isWished;
        private List<String> labels;
        private List<Integer> currentUserData;
    }

    @Data
    @Builder
    public static class ComparisonChart {
        private Long compareId;
        private String compareName;
        private List<Integer> recommendedProductData;
    }

    @Data
    @Builder
    public static class MaturityInfo {
        private String coverageDesc;
        private String note;
    }
}