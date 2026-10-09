package com.pfm.dao;

import com.pfm.exception.DataAccessException;
import com.pfm.model.Interaction;
import com.pfm.util.DBConnectionUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InteractionDao implements Dao<Interaction> {

    private static final String INSERT =
            "INSERT INTO interactions (advisor_id, user_id, message) VALUES (?, ?, ?)";
    private static final String SELECT_BY_ID = "SELECT * FROM interactions WHERE id = ?";
    private static final String SELECT_BY_USER = "SELECT * FROM interactions WHERE user_id = ?";
    private static final String SELECT_BY_ADVISOR = "SELECT * FROM interactions WHERE advisor_id = ?";
    private static final String SELECT_ALL = "SELECT * FROM interactions";
    private static final String UPDATE = "UPDATE interactions SET message = ? WHERE id = ?";
    private static final String DELETE = "DELETE FROM interactions WHERE id = ?";

    @Override
    public void save(Interaction interaction) {
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, interaction.getAdvisorId());
            ps.setInt(2, interaction.getUserId());
            ps.setString(3, interaction.getMessage());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    interaction.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to save interaction", e);
        }
    }

    @Override
    public Interaction findById(int id) {
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find interaction " + id, e);
        }
    }

    public List<Interaction> findByUserId(int userId) {
        return queryList(SELECT_BY_USER, userId, "Failed to list interactions for user " + userId);
    }

    public List<Interaction> findByAdvisorId(int advisorId) {
        return queryList(SELECT_BY_ADVISOR, advisorId, "Failed to list interactions for advisor " + advisorId);
    }

    @Override
    public List<Interaction> findAll() {
        List<Interaction> list = new ArrayList<>();
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to list interactions", e);
        }
    }

    @Override
    public void update(Interaction interaction) {
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE)) {
            ps.setString(1, interaction.getMessage());
            ps.setInt(2, interaction.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update interaction " + interaction.getId(), e);
        }
    }

    @Override
    public void delete(int id) {
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete interaction " + id, e);
        }
    }

    private List<Interaction> queryList(String sql, int param, String errorMessage) {
        List<Interaction> list = new ArrayList<>();
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

    private Interaction mapRow(ResultSet rs) throws SQLException {
        Interaction i = new Interaction(rs.getInt("id"), rs.getInt("advisor_id"),
                rs.getInt("user_id"), rs.getString("message"));
        Timestamp ts = rs.getTimestamp("timestamp");
        if (ts != null) {
            i.setTimestamp(ts.toLocalDateTime());
        }
        return i;
    }
}