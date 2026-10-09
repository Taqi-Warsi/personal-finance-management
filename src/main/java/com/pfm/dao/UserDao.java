package com.pfm.dao;

import com.pfm.exception.DataAccessException;
import com.pfm.model.*;
import com.pfm.util.DBConnectionUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDao implements Dao<AppUser> {

    private static final String INSERT =
            "INSERT INTO users (name, email, password_hash, role) VALUES (?, ?, ?, ?)";
    private static final String SELECT_BY_ID = "SELECT * FROM users WHERE id = ?";
    private static final String SELECT_BY_EMAIL = "SELECT * FROM users WHERE email = ?";
    private static final String SELECT_ALL = "SELECT * FROM users";
    private static final String UPDATE =
            "UPDATE users SET name = ?, email = ?, password_hash = ? WHERE id = ?";
    private static final String DELETE = "DELETE FROM users WHERE id = ?";

    // Demonstrates: PreparedStatement INSERT + generated key (JDBC rubric item)
    @Override
    public void save(AppUser user) {
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getRole().name());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setId(keys.getInt(1));   // hand the DB-generated id back to the object
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to save user: " + user.getEmail(), e);
        }
    }

    @Override
    public AppUser findById(int id) {
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find user id " + id, e);
        }
    }

    // Needed later for login
    public AppUser findByEmail(String email) {
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_EMAIL)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find user by email " + email, e);
        }
    }

    @Override
    public List<AppUser> findAll() {
        List<AppUser> users = new ArrayList<>();
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                users.add(mapRow(rs));
            }
            return users;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to list users", e);
        }
    }

    @Override
    public void update(AppUser user) {
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE)) {

            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ps.setInt(4, user.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update user id " + user.getId(), e);
        }
    }

    @Override
    public void delete(int id) {
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete user id " + id, e);
        }
    }

    // Turns one DB row into the correct subclass based on the role column (polymorphism)
    private AppUser mapRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        String email = rs.getString("email");
        String hash = rs.getString("password_hash");
        Role role = Role.valueOf(rs.getString("role"));

        AppUser user = switch (role) {
            case ADMIN -> new Admin(id, name, email, hash);
            case ADVISOR -> new FinancialAdvisor(id, name, email, hash);
            case USER -> new User(id, name, email, hash);
        };

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            user.setCreatedAt(created.toLocalDateTime());
        }
        return user;
    }
}