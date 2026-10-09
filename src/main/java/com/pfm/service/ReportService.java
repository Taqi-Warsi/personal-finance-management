package com.pfm.service;

import com.pfm.model.Budget;
import com.pfm.model.Expense;
import com.pfm.exception.InsufficientDataException;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class ReportService {

    // Map<String, List<Expense>>
    public Map<String, List<Expense>> groupByCategory(List<Expense> expenses) {
        return expenses.stream().collect(Collectors.groupingBy(Expense::getCategory));
    }

    // Map<String, BigDecimal>
    public Map<String, BigDecimal> totalByCategory(List<Expense> expenses) {
        return expenses.stream().collect(Collectors.groupingBy(
                Expense::getCategory,
                Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)));
    }

    public Map.Entry<String, BigDecimal> topCategory(List<Expense> expenses)
            throws InsufficientDataException {
        if (expenses == null || expenses.isEmpty()) {
            throw new InsufficientDataException("No expenses recorded, so there is no top category");
        }
        return totalByCategory(expenses).entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElseThrow(() -> new InsufficientDataException("Could not determine top category"));
    }

    // Set<String>, sorted
    public Set<String> uniqueCategories(List<Expense> expenses) {
        return expenses.stream()
                .map(Expense::getCategory)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    // List<Budget> sorted with a Comparator, highest usage first
    public List<Budget> budgetsByUsage(List<Budget> budgets) {
        return budgets.stream()
                .sorted(Comparator.comparing(Budget::getUsagePercent, Comparator.reverseOrder()))
                .collect(Collectors.toList());
    }
}
