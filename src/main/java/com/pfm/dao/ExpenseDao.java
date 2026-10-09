package com.pfm.dao;

import com.pfm.exception.DataAccessException;
import com.pfm.model.Expense;
import com.pfm.util.DBConnectionUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExpenseDao implements Dao<Expense> {

    private static final String INSERT =
            "INSERT INTO expenses (user_id, category, amount, expense_date, note) VALUES (?, ?, ?, ?, ?)";
    private static final String SELECT_BY_ID = "SELECT * FROM expenses WHERE id = ?";
    private static final String SELECT_BY_USER = "SELECT * FROM expenses WHERE user_id = ?";
    private static final String SELECT_ALL = "SELECT * FROM expenses";
    private static final String UPDATE =
            "UPDATE expenses SET category = ?, amount = ?, expense_date = ?, note = ? WHERE id = ?";
    private static final String DELETE = "DELETE FROM expenses WHERE id = ?";

    @Override
    public void save(Expense expense) {
        try (Connection conn = DBConnectionUtil.getConnection()) {
            save(conn, expense);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to save expense", e);
        }
    }

    // Uses a connection supplied by the caller so it can join a transaction
    public void save(Connection conn, Expense expense) {
        try (PreparedStatement ps = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, expense.getUserId());
            ps.setString(2, expense.getCategory());
            ps.setBigDecimal(3, expense.getAmount());
            ps.setDate(4, Date.valueOf(expense.getExpenseDate()));
            ps.setString(5, expense.getNote());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    expense.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to save expense", e);
        }
    }

    @Override
    public Expense findById(int id) {
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find expense " + id, e);
        }
    }

    public List<Expense> findByUserId(int userId) {
        List<Expense> list = new ArrayList<>();
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
            throw new DataAccessException("Failed to list expenses for user " + userId, e);
        }
    }

    @Override
    public List<Expense> findAll() {
        List<Expense> list = new ArrayList<>();
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to list expenses", e);
        }
    }

    @Override
    public void update(Expense expense) {
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE)) {
            ps.setString(1, expense.getCategory());
            ps.setBigDecimal(2, expense.getAmount());
            ps.setDate(3, Date.valueOf(expense.getExpenseDate()));
            ps.setString(4, expense.getNote());
            ps.setInt(5, expense.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update expense " + expense.getId(), e);
        }
    }

    @Override
    public void delete(int id) {
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete expense " + id, e);
        }
    }

    private Expense mapRow(ResultSet rs) throws SQLException {
        return new Expense(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getString("category"),
                rs.getBigDecimal("amount"),
                rs.getDate("expense_date").toLocalDate(),
                rs.getString("note"));
    }
}