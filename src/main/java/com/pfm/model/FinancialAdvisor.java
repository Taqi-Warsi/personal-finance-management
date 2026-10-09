package com.pfm.model;

import java.util.ArrayList;
import java.util.List;

public class FinancialAdvisor extends AppUser implements Notifiable, Reportable {

    private final List<String> notifications = new ArrayList<>();

    public FinancialAdvisor(int id, String name, String email, String passwordHash) {
        super(id, name, email, passwordHash, Role.ADVISOR);
    }

    @Override
    public String getDashboardSummary() {
        return "Advisor Dashboard: Advice Management, User Interaction, "
                + "User Progress Tracking, Advice History, Interaction Statistics";
    }

    @Override
    public List<String> getPermissions() {
        return List.of("GIVE_ADVICE", "MESSAGE_USERS", "VIEW_USER_PROGRESS", "VIEW_ADVICE_HISTORY");
    }

    @Override
    public void sendNotification(String message) {
        notifications.add(message);
    }

    public List<String> getNotifications() {
        return notifications;
    }

    @Override
    public String generateReport() {
        return "Advisor activity report for " + getName();
    }
}