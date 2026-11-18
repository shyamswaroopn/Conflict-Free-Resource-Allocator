package com.allocator.dao;

import com.allocator.config.DatabaseConnection;
import com.allocator.model.Allocation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Resource & Time Slot Allocations via MySQL JDBC.
 */
public class AllocationDAO {

    public List<Allocation> findAllDetailed() throws SQLException {
        List<Allocation> allocations = new ArrayList<>();
        String sql =
            "SELECT a.id, a.task_id, t.code AS task_code, t.title AS task_title, t.instructor, " +
            "       (SELECT COUNT(*) FROM task_enrollments te WHERE te.task_id = t.id) AS student_count, " +
            "       a.color_index, a.time_slot_label, a.resource_id, " +
            "       r.code AS resource_code, r.name AS resource_name, r.capacity AS resource_capacity, " +
            "       a.algorithm_used, a.is_manual_override " +
            "FROM allocations a " +
            "JOIN tasks t ON a.task_id = t.id " +
            "JOIN resources r ON a.resource_id = r.id " +
            "ORDER BY a.color_index ASC, r.code ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                allocations.add(new Allocation(
                    rs.getInt("id"),
                    rs.getInt("task_id"),
                    rs.getString("task_code"),
                    rs.getString("task_title"),
                    rs.getString("instructor"),
                    rs.getInt("student_count"),
                    rs.getInt("color_index"),
                    rs.getString("time_slot_label"),
                    rs.getInt("resource_id"),
                    rs.getString("resource_code"),
                    rs.getString("resource_name"),
                    rs.getInt("resource_capacity"),
                    rs.getString("algorithm_used"),
                    rs.getBoolean("is_manual_override")
                ));
            }
        }
        return allocations;
    }

    public void saveBatchAllocations(List<Allocation> allocations) throws SQLException {
        String deleteAllSql = "DELETE FROM allocations";
        String insertSql = "INSERT INTO allocations (task_id, color_index, time_slot_label, resource_id, algorithm_used, is_manual_override) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate(deleteAllSql);
                }

                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    for (Allocation alloc : allocations) {
                        ps.setInt(1, alloc.getTaskId());
                        ps.setInt(2, alloc.getColorIndex());
                        ps.setString(3, alloc.getTimeSlotLabel());
                        ps.setInt(4, alloc.getResourceId());
                        ps.setString(5, alloc.getAlgorithmUsed());
                        ps.setBoolean(6, alloc.isManualOverride());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public boolean updateManualAllocation(int allocationId, int newColorIndex, String newTimeSlotLabel, int newResourceId) throws SQLException {
        String sql = "UPDATE allocations SET color_index = ?, time_slot_label = ?, resource_id = ?, is_manual_override = TRUE WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newColorIndex);
            ps.setString(2, newTimeSlotLabel);
            ps.setInt(3, newResourceId);
            ps.setInt(4, allocationId);
            return ps.executeUpdate() > 0;
        }
    }

    public void clearAll() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM allocations");
        }
    }
}
