package com.pfm.dao;

import com.pfm.exception.DataAccessException;
import com.pfm.model.Feedback;
import com.pfm.util.DBConnectionUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FeedbackDao implements Dao<Feedback> {

    private static final String INSERT =
            "INSERT INTO feedback (user_id, message, status) VALUES (?, ?, ?)";
    private static final String SELECT_BY_ID = "SELECT * FROM feedback WHERE id = ?";
    private static final String SELECT_BY_USER = "SELECT * FROM feedback WHERE user_id = ?";
    private static final String SELECT_BY_STATUS = "SELECT * FROM feedback WHERE status = ?";
    private static final String SELECT_ALL = "SELECT * FROM feedback";
    private static final String UPDATE = "UPDATE feedback SET message = ?, status = ? WHERE id = ?";
    private static final String DELETE = "DELETE FROM feedback WHERE id = ?";

    @Override
    public void save(Feedback feedback) {
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, feedback.getUserId());
            ps.setString(2, feedback.getMessage());
            ps.setString(3, feedback.getStatus());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    feedback.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to save feedback", e);
        }
    }

    @Override
    public Feedback findById(int id) {
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find feedback " + id, e);
        }
    }

    public List<Feedback> findByUserId(int userId) {
        List<Feedback> list = new ArrayList<>();
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
            throw new DataAccessException("Failed to list feedback for user " + userId, e);
        }
    }

    // For the admin's "User Feedback" table (e.g. all OPEN tickets)
    public List<Feedback> findByStatus(String status) {
        List<Feedback> list = new ArrayList<>();
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_STATUS)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to list feedback with status " + status, e);
        }
    }

    @Override
    public List<Feedback> findAll() {
        List<Feedback> list = new ArrayList<>();
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to list feedback", e);
        }
    }

    @Override
    public void update(Feedback feedback) {
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE)) {
            ps.setString(1, feedback.getMessage());
            ps.setString(2, feedback.getStatus());
            ps.setInt(3, feedback.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update feedback " + feedback.getId(), e);
        }
    }

    @Override
    public void delete(int id) {
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete feedback " + id, e);
        }
    }

    private Feedback mapRow(ResultSet rs) throws SQLException {
        Feedback f = new Feedback(rs.getInt("id"), rs.getInt("user_id"), rs.getString("message"));
        f.setStatus(rs.getString("status"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            f.setCreatedAt(ts.toLocalDateTime());
        }
        return f;
    }
}