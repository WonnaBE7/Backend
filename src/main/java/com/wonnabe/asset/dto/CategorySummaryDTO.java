package com.wonnabe.asset.dto;

public class CategorySummaryDTO {
    private String consumptionCategory;  // DB 컬럼명과 일치
    private String amount;
    private Double percentage;  // 비율 계산 후 세팅할 필드 추가

    public String getConsumptionCategory() {
        return consumptionCategory;
    }

    public void setConsumptionCategory(String consumptionCategory) {
        this.consumptionCategory = consumptionCategory;
    }

    public String getAmount() {
        return amount;
    }
    public void setAmount(String amount) {
        this.amount = amount;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }
}