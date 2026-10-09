package com.pfm.model;

import java.time.LocalDateTime;
import java.util.List;

public abstract class AppUser {

    private int id;
    private String name;
    private String email;
    private String passwordHash;
    private Role role;
    private LocalDateTime createdAt;

    protected AppUser(int id, String name, String email, String passwordHash, Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.createdAt = LocalDateTime.now();
    }

    // Polymorphism: every role implements these differently
    public abstract String getDashboardSummary();

    public abstract List<String> getPermissions();

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Role getRole() { return role; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return role + " [id=" + id + ", name=" + name + ", email=" + email + "]";
    }
}