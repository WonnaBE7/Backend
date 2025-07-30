package com.wonnabe.asset.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class CategorySummaryDTO {
    private String consumptionCategory;  // DB 컬럼명과 일치
    private double amount;
    private double percentage;
    private double diffFromLastMonth;

    @JsonIgnore
    private double diffFromYesterday;

    public String getConsumptionCategory() {
        return consumptionCategory;
    }

    public void setConsumptionCategory(String consumptionCategory) {
        this.consumptionCategory = consumptionCategory;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public double getDiffFromLastMonth() {
        return diffFromLastMonth;
    }

    public void setDiffFromLastMonth(double diffFromLastMonth) {
        this.diffFromLastMonth = diffFromLastMonth;
    }

    public double getDiffFromYesterday() {
        return diffFromYesterday;
    }

    public void setDiffFromYesterday(double diffFromYesterday) {
        this.diffFromYesterday = diffFromYesterday;
    }
}
