package com.pfm;

import com.pfm.dao.*;
import com.pfm.exception.*;
import com.pfm.model.*;
import com.pfm.service.AccessControl;
import com.pfm.service.ExpenseService;
import com.pfm.service.ReportService;
import com.pfm.thread.BudgetMonitor;
import com.pfm.thread.SyncDemo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws Exception {
        UserDao userDao = new UserDao();
        ExpenseDao expenseDao = new ExpenseDao();
        BudgetDao budgetDao = new BudgetDao();
        AdviceDao adviceDao = new AdviceDao();
        FeedbackDao feedbackDao = new FeedbackDao();
        InteractionDao interactionDao = new InteractionDao();
        ExpenseService service = new ExpenseService();
        ReportService reports = new ReportService();

        long stamp = System.currentTimeMillis();
        User user = new User(0, "Demo User", "demo" + stamp + "@pfm.com", "hash");
        FinancialAdvisor advisor = new FinancialAdvisor(0, "Demo Advisor", "advisor" + stamp + "@pfm.com", "hash");
        userDao.save(user);
        userDao.save(advisor);
        int uid = user.getId();

        try {
            String month = YearMonth.now().toString();
            LocalDate today = LocalDate.now();
            Budget food = new Budget(0, uid, "Food", month, new BigDecimal("1000"));
            Budget travel = new Budget(0, uid, "Travel", month, new BigDecimal("500"));
            budgetDao.save(food);
            budgetDao.save(travel);

            System.out.println("=== 1. TRANSACTION SUCCESS ===");
            service.addExpense(new Expense(0, uid, "Food", new BigDecimal("400"), today, "Groceries"));
            service.addExpense(new Expense(0, uid, "Food", new BigDecimal("350"), today, "Dinner"));
            service.addExpense(new Expense(0, uid, "Travel", new BigDecimal("450"), today, "Cab"));
            System.out.println("Food spent in DB: " + budgetDao.findById(food.getId()).getSpentAmount()
                    + " (expected 750.00)");

            System.out.println("\n=== 2. TRANSACTION ROLLBACK (simulated failure) ===");
            int before = expenseDao.findByUserId(uid).size();
            try {
                service.addExpense(new Expense(0, uid, "Food", new BigDecimal("100"), today, "Fails"), true);
            } catch (RuntimeException e) {
                System.out.println("Caught: " + e.getMessage());
            }
            int after = expenseDao.findByUserId(uid).size();
            System.out.println("Expense rows before/after: " + before + " / " + after + " (should match)");
            System.out.println("Food spent still: " + budgetDao.findById(food.getId()).getSpentAmount());

            System.out.println("\n=== 3. OVERSPEND BLOCKED (business rule) ===");
            try {
                service.addExpense(new Expense(0, uid, "Food", new BigDecimal("300"), today, "Too much"));
                System.out.println("ERROR: this should have been blocked");
            } catch (InvalidBudgetException e) {
                System.out.println("Blocked: " + e.getMessage());
            }
            System.out.println("Expense rows still: " + expenseDao.findByUserId(uid).size()
                    + ", Food spent still: " + budgetDao.findById(food.getId()).getSpentAmount());

            System.out.println("\n=== 4. COLLECTIONS AND GENERICS ===");
            List<Expense> expenses = expenseDao.findByUserId(uid);
            System.out.println("Categories: " + reports.uniqueCategories(expenses));
            System.out.println("Grouped: " + reports.groupByCategory(expenses).keySet());
            System.out.println("Totals: " + reports.totalByCategory(expenses));
            System.out.println("Top category: " + reports.topCategory(expenses).getKey());
            System.out.println("Budgets by usage: " + reports.budgetsByUsage(budgetDao.findByUserId(uid)));

            System.out.println("\n=== 5. BACKGROUND MONITOR THREAD ===");
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            scheduler.scheduleAtFixedRate(new BudgetMonitor(budgetDao), 0, 2, TimeUnit.SECONDS);
            Thread.sleep(3000);
            scheduler.shutdown();

            System.out.println("\n=== 6. SYNCHRONIZATION DEMO (correct total = 100000) ===");
            System.out.println("Without synchronized: " + SyncDemo.run(false));
            System.out.println("With synchronized:    " + SyncDemo.run(true));

            System.out.println("\n=== 7. ADVICE, FEEDBACK, INTERACTION DAOs ===");
            Advice advice = new Advice(0, advisor.getId(), uid, "Cut dining out by 10%");
            adviceDao.save(advice);
            advice.setMessage("Cut dining out by 15%");
            adviceDao.update(advice);
            System.out.println("Advice for user: " + adviceDao.findByUserId(uid));

            Feedback feedback = new Feedback(0, uid, "Please add CSV export");
            feedbackDao.save(feedback);
            feedback.setStatus("RESOLVED");
            feedbackDao.update(feedback);
            System.out.println("Feedback: " + feedbackDao.findById(feedback.getId()));
            System.out.println("OPEN tickets for this user: "
                    + feedbackDao.findByStatus("OPEN").stream().filter(f -> f.getUserId() == uid).count());

            Interaction msg = new Interaction(0, advisor.getId(), uid, "Let's review your budget this week");
            interactionDao.save(msg);
            System.out.println("Interactions: " + interactionDao.findByUserId(uid));

            System.out.println("\n=== 8. CUSTOM EXCEPTIONS ===");
            try {
                reports.topCategory(List.of());
            } catch (InsufficientDataException e) {
                System.out.println("InsufficientDataException: " + e.getMessage());
            }
            Admin admin = new Admin(0, "Demo Admin", "admin@pfm.com", "hash");
            AccessControl.require(admin, "MANAGE_USERS");
            System.out.println("Admin allowed to MANAGE_USERS");
            try {
                AccessControl.require(user, "MANAGE_USERS");
            } catch (UnauthorizedAccessException e) {
                System.out.println("UnauthorizedAccessException: " + e.getMessage());
            }
        } finally {
            userDao.delete(uid);              // cascades to expenses, budgets, advice, feedback, interactions
            userDao.delete(advisor.getId());
            System.out.println("\nDemo data cleaned up.");
        }
    }
}