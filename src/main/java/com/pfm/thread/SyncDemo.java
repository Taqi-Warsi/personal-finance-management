package com.pfm.thread;

import com.pfm.model.Budget;

import java.math.BigDecimal;

public class SyncDemo {

    // Two threads each add 1 to the same budget 50,000 times. Correct total: 100,000.
    public static BigDecimal run(boolean useSynchronization) throws InterruptedException {
        Budget budget = new Budget(0, 0, "Demo", "2026-10", new BigDecimal("1000000"));

        Runnable task = () -> {
            for (int i = 0; i < 50_000; i++) {
                if (useSynchronization) {
                    synchronized (budget) {
                        increment(budget);
                    }
                } else {
                    increment(budget);
                }
            }
        };

        Thread t1 = new Thread(task, "worker-1");
        Thread t2 = new Thread(task, "worker-2");
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        return budget.getSpentAmount();
    }

    private static void increment(Budget b) {
        b.setSpentAmount(b.getSpentAmount().add(BigDecimal.ONE));   // read, add, write: not atomic
    }
}