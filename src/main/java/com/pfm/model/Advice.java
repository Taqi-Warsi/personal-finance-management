package com.pfm.model;

import java.time.LocalDateTime;

public class Advice {

    private int id;
    private int advisorId;
    private int userId;
    private String message;
    private LocalDateTime createdAt;

    public Advice(int id, int advisorId, int userId, String message) {
        this.id = id;
        this.advisorId = advisorId;
        this.userId = userId;
        this.message = message;
        this.createdAt = LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getAdvisorId() { return advisorId; }
    public void setAdvisorId(int advisorId) { this.advisorId = advisorId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Advice [advisor=" + advisorId + " -> user=" + userId + ", " + message + "]";
    }
}