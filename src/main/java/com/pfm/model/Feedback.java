package com.pfm.model;

import java.time.LocalDateTime;

public class Feedback {

    private int id;
    private int userId;
    private String message;
    private String status;          // OPEN, IN_PROGRESS, RESOLVED
    private LocalDateTime createdAt;

    public Feedback(int id, int userId, String message) {
        this.id = id;
        this.userId = userId;
        this.message = message;
        this.status = "OPEN";
        this.createdAt = LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Feedback [user=" + userId + ", status=" + status + ", " + message + "]";
    }
}