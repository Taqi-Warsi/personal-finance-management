package com.pfm.dao;

import com.pfm.exception.DataAccessException;
import com.pfm.model.Advice;
import com.pfm.util.DBConnectionUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdviceDao implements Dao<Advice> {

    private static final String INSERT =
            "INSERT INTO advice (advisor_id, user_id, message) VALUES (?, ?, ?)";
    private static final String SELECT_BY_ID = "SELECT * FROM advice WHERE id = ?";
    private static final String SELECT_BY_USER =
            "SELECT * FROM advice WHERE user_id = ? ORDER BY created_at DESC";
    private static final String SELECT_BY_ADVISOR =
            "SELECT * FROM advice WHERE advisor_id = ? ORDER BY created_at DESC";
    private static final String SELECT_ALL = "SELECT * FROM advice";
    private static final String UPDATE = "UPDATE advice SET message = ? WHERE id = ?";
    private static final String DELETE = "DELETE FROM advice WHERE id = ?";

    @Override
    public void save(Advice advice) {
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, advice.getAdvisorId());
            ps.setInt(2, advice.getUserId());
            ps.setString(3, advice.getMessage());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    advice.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to save advice", e);
        }
    }

    @Override
    public Advice findById(int id) {
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find advice " + id, e);
        }
    }

    public List<Advice> findByUserId(int userId) {
        return queryList(SELECT_BY_USER, userId, "Failed to list advice for user " + userId);
    }

    public List<Advice> findByAdvisorId(int advisorId) {
        return queryList(SELECT_BY_ADVISOR, advisorId, "Failed to list advice by advisor " + advisorId);
    }

    @Override
    public List<Advice> findAll() {
        List<Advice> list = new ArrayList<>();
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to list advice", e);
        }
    }

    @Override
    public void update(Advice advice) {
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE)) {
            ps.setString(1, advice.getMessage());
            ps.setInt(2, advice.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update advice " + advice.getId(), e);
        }
    }

    @Override
    public void delete(int id) {
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete advice " + id, e);
        }
    }

    private List<Advice> queryList(String sql, int param, String errorMessage) {
        List<Advice> list = new ArrayList<>();
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException(errorMessage, e);
        }
    }

    private Advice mapRow(ResultSet rs) throws SQLException {
        Advice a = new Advice(rs.getInt("id"), rs.getInt("advisor_id"),
                rs.getInt("user_id"), rs.getString("message"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            a.setCreatedAt(ts.toLocalDateTime());
        }
        return a;
    }
}