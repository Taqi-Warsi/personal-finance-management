package com.pfm.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Budget {

    private int id;
    private int userId;
    private String category;
    private String month;              // format: "2026-10"
    private BigDecimal limitAmount;
    private BigDecimal spentAmount;

    public Budget(int id, int userId, String category, String month, BigDecimal limitAmount) {
        this.id = id;
        this.userId = userId;
        this.category = category;
        this.month = month;
        this.limitAmount = limitAmount;
        this.spentAmount = BigDecimal.ZERO;
    }

    public boolean isExceeded() {
        return spentAmount.compareTo(limitAmount) > 0;
    }

    public BigDecimal getUsagePercent() {
        if (limitAmount.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return spentAmount.multiply(BigDecimal.valueOf(100))
                .divide(limitAmount, 2, RoundingMode.HALF_UP);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }

    public BigDecimal getLimitAmount() { return limitAmount; }
    public void setLimitAmount(BigDecimal limitAmount) { this.limitAmount = limitAmount; }

    public BigDecimal getSpentAmount() { return spentAmount; }
    public void setSpentAmount(BigDecimal spentAmount) { this.spentAmount = spentAmount; }

    @Override
    public String toString() {
        return "Budget [" + category + ", " + month + ", spent " + spentAmount + "/" + limitAmount + "]";
    }
}