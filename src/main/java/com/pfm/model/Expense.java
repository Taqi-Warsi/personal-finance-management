package com.pfm.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Expense {

    private int id;
    private int userId;
    private String category;
    private BigDecimal amount;
    private LocalDate expenseDate;
    private String note;

    public Expense(int id, int userId, String category, BigDecimal amount,
                   LocalDate expenseDate, String note) {
        this.id = id;
        this.userId = userId;
        this.category = category;
        this.amount = amount;
        this.expenseDate = expenseDate;
        this.note = note;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDate getExpenseDate() { return expenseDate; }
    public void setExpenseDate(LocalDate expenseDate) { this.expenseDate = expenseDate; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    @Override
    public String toString() {
        return "Expense [" + category + ", " + amount + ", " + expenseDate + "]";
    }
}