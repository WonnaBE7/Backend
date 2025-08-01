package com.wonnabe.asset.domain;

public class SummariesCacheV0 {
    private Long id;
    private String userId;
    private String periodType;         // 'daily' or 'monthly'
    private String date;               // YYYY-MM-DD (daily만)
    private String yearMonth;          // YYYY-MM (monthly만)
    private String consumptionCategory;
    private Double amount;
    private String updatedAt;

    // Getter/Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getPeriodType() { return periodType; }
    public void setPeriodType(String periodType) { this.periodType = periodType; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getYearMonth() { return yearMonth; }
    public void setYearMonth(String yearMonth) { this.yearMonth = yearMonth; }

    public String getConsumptionCategory() { return consumptionCategory; }
    public void setConsumptionCategory(String consumptionCategory) { this.consumptionCategory = consumptionCategory; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
