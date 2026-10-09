package com.pfm.service;

import com.pfm.dao.BudgetDao;
import com.pfm.dao.ExpenseDao;
import com.pfm.exception.DataAccessException;
import com.pfm.exception.InvalidBudgetException;
import com.pfm.model.Budget;
import com.pfm.model.Expense;
import com.pfm.util.DBConnectionUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.YearMonth;

public class ExpenseService {

    private static final Object LOCK = new Object();

    private final ExpenseDao expenseDao = new ExpenseDao();
    private final BudgetDao budgetDao = new BudgetDao();

    public Budget addExpense(Expense expense) throws InvalidBudgetException {
        return addExpense(expense, false);
    }

    // Demonstrates: transaction management (setAutoCommit / commit / rollback)
    // and synchronization (one expense-and-budget update at a time).
    // simulateFailure exists only so the rollback can be demoed.
    public Budget addExpense(Expense expense, boolean simulateFailure) throws InvalidBudgetException {
        if (expense.getAmount() == null || expense.getAmount().signum() <= 0) {
            throw new InvalidBudgetException("Expense amount must be positive");
        }

        synchronized (LOCK) {
            try (Connection conn = DBConnectionUtil.getConnection()) {
                conn.setAutoCommit(false);
                try {
                    String month = YearMonth.from(expense.getExpenseDate()).toString();
                    Budget budget = budgetDao.findByUserCategoryMonth(
                            conn, expense.getUserId(), expense.getCategory(), month);

                    // Business rule: an expense may not push spending past the budget limit
                    if (budget != null) {
                        BigDecimal projected = budget.getSpentAmount().add(expense.getAmount());
                        if (projected.compareTo(budget.getLimitAmount()) > 0) {
                            throw new InvalidBudgetException("Expense of " + expense.getAmount()
                                    + " would exceed the " + expense.getCategory() + " budget (limit "
                                    + budget.getLimitAmount() + ", already spent " + budget.getSpentAmount() + ")");
                        }
                    }

                    expenseDao.save(conn, expense);

                    if (simulateFailure) {
                        throw new IllegalStateException("Simulated failure after expense insert");
                    }

                    if (budget != null) {
                        budgetDao.addToSpent(conn, budget.getId(), expense.getAmount());
                        budget.setSpentAmount(budget.getSpentAmount().add(expense.getAmount()));
                    }

                    conn.commit();
                    return budget;       // null if the user has no budget for this category/month
                } catch (SQLException | RuntimeException | InvalidBudgetException e) {
                    conn.rollback();     // undo everything done in this transaction
                    throw e;
                }
            } catch (SQLException e) {
                throw new DataAccessException("Transaction failed", e);
            }
        }
    }
}
