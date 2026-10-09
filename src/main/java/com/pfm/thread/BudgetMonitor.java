package com.pfm.thread;

import com.pfm.dao.BudgetDao;
import com.pfm.model.Budget;

import java.math.BigDecimal;

public class BudgetMonitor implements Runnable {

    private static final BigDecimal ALERT_THRESHOLD = new BigDecimal("80");

    private final BudgetDao budgetDao;

    public BudgetMonitor(BudgetDao budgetDao) {
        this.budgetDao = budgetDao;
    }

    @Override
    public void run() {
        try {
            for (Budget b : budgetDao.findAll()) {
                if (b.getUsagePercent().compareTo(ALERT_THRESHOLD) >= 0) {
                    System.out.println("[" + Thread.currentThread().getName() + "] ALERT: user "
                            + b.getUserId() + " has used " + b.getUsagePercent() + "% of the "
                            + b.getCategory() + " budget (" + b.getMonth() + ")");
                }
            }
        } catch (RuntimeException e) {
            // Without this, one failure would silently cancel all future scheduled runs
            System.err.println("Budget monitor scan failed: " + e.getMessage());
        }
    }
}