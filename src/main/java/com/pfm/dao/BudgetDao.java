package com.pfm.dao;

import com.pfm.exception.DataAccessException;
import com.pfm.model.Budget;
import com.pfm.util.DBConnectionUtil;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BudgetDao implements Dao<Budget> {

    private static final String INSERT =
            "INSERT INTO budgets (user_id, category, month, limit_amount, spent_amount) VALUES (?, ?, ?, ?, ?)";
    private static final String SELECT_BY_ID = "SELECT * FROM budgets WHERE id = ?";
    private static final String SELECT_BY_USER = "SELECT * FROM budgets WHERE user_id = ?";
    private static final String SELECT_ALL = "SELECT * FROM budgets";
    private static final String SELECT_BY_KEY =
            "SELECT * FROM budgets WHERE user_id = ? AND category = ? AND month = ?";
    private static final String UPDATE =
            "UPDATE budgets SET category = ?, month = ?, limit_amount = ?, spent_amount = ? WHERE id = ?";
    private static final String ADD_SPENT =
            "UPDATE budgets SET spent_amount = spent_amount + ? WHERE id = ?";
    private static final String DELETE = "DELETE FROM budgets WHERE id = ?";

    @Override
    public void save(Budget budget) {
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, budget.getUserId());
            ps.setString(2, budget.getCategory());
            ps.setString(3, budget.getMonth());
            ps.setBigDecimal(4, budget.getLimitAmount());
            ps.setBigDecimal(5, budget.getSpentAmount());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    budget.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to save budget", e);
        }
    }

    @Override
    public Budget findById(int id) {
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find budget " + id, e);
        }
    }

    public List<Budget> findByUserId(int userId) {
        List<Budget> list = new ArrayList<>();
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_USER)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to list budgets for user " + userId, e);
        }
    }

    // Transaction-aware: runs on the caller's connection
    public Budget findByUserCategoryMonth(Connection conn, int userId, String category, String month) {
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_KEY)) {
            ps.setInt(1, userId);
            ps.setString(2, category);
            ps.setString(3, month);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to look up budget", e);
        }
    }

    // Transaction-aware: the database does the addition itself
    public void addToSpent(Connection conn, int budgetId, BigDecimal amount) {
        try (PreparedStatement ps = conn.prepareStatement(ADD_SPENT)) {
            ps.setBigDecimal(1, amount);
            ps.setInt(2, budgetId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update spent amount for budget " + budgetId, e);
        }
    }

    @Override
    public List<Budget> findAll() {
        List<Budget> list = new ArrayList<>();
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to list budgets", e);
        }
    }

    @Override
    public void update(Budget budget) {
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE)) {
            ps.setString(1, budget.getCategory());
            ps.setString(2, budget.getMonth());
            ps.setBigDecimal(3, budget.getLimitAmount());
            ps.setBigDecimal(4, budget.getSpentAmount());
            ps.setInt(5, budget.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update budget " + budget.getId(), e);
        }
    }

    @Override
    public void delete(int id) {
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete budget " + id, e);
        }
    }

    private Budget mapRow(ResultSet rs) throws SQLException {
        Budget b = new Budget(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getString("category"),
                rs.getString("month"),
                rs.getBigDecimal("limit_amount"));
        b.setSpentAmount(rs.getBigDecimal("spent_amount"));
        return b;
    }
}