package com.pfm.model;

import java.util.ArrayList;
import java.util.List;

public class User extends AppUser implements Notifiable {

    private final List<Expense> expenses = new ArrayList<>();
    private final List<Budget> budgets = new ArrayList<>();
    private final List<String> notifications = new ArrayList<>();

    public User(int id, String name, String email, String passwordHash) {
        super(id, name, email, passwordHash, Role.USER);
    }

    @Override
    public String getDashboardSummary() {
        return "User Dashboard: Expense Tracker, Budget Overview, "
                + "Financial Advice, Profile Management, Financial Reports";
    }

    @Override
    public List<String> getPermissions() {
        return List.of("TRACK_EXPENSES", "SET_BUDGETS", "VIEW_ADVICE",
                "EDIT_PROFILE", "VIEW_REPORTS");
    }

    @Override
    public void sendNotification(String message) {
        notifications.add(message);
    }

    public List<Expense> getExpenses() { return expenses; }
    public List<Budget> getBudgets() { return budgets; }
    public List<String> getNotifications() { return notifications; }
}