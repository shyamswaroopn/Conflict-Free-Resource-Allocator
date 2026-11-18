package com.allocator.dao;

import com.allocator.config.DatabaseConnection;
import com.allocator.model.Resource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Physical Resources (Classrooms, Labs, Auditoriums) via MySQL JDBC.
 */
public class ResourceDAO {

    public List<Resource> findAll() throws SQLException {
        List<Resource> resources = new ArrayList<>();
        String sql = "SELECT id, code, name, type, capacity, building, is_available FROM resources ORDER BY code ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                resources.add(mapRow(rs));
            }
        }
        return resources;
    }

    public List<Resource> findAvailableResources() throws SQLException {
        List<Resource> resources = new ArrayList<>();
        String sql = "SELECT id, code, name, type, capacity, building, is_available FROM resources WHERE is_available = TRUE ORDER BY capacity ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                resources.add(mapRow(rs));
            }
        }
        return resources;
    }

    public Resource insert(Resource resource) throws SQLException {
        String sql = "INSERT INTO resources (code, name, type, capacity, building, is_available) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, resource.getCode().trim());
            ps.setString(2, resource.getName().trim());
            ps.setString(3, resource.getType().name());
            ps.setInt(4, resource.getCapacity());
            ps.setString(5, resource.getBuilding().trim());
            ps.setBoolean(6, resource.isAvailable());

            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    resource.setId(keys.getInt(1));
                }
            }
        }
        return resource;
    }

    public boolean update(Resource resource) throws SQLException {
        String sql = "UPDATE resources SET code = ?, name = ?, type = ?, capacity = ?, building = ?, is_available = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, resource.getCode().trim());
            ps.setString(2, resource.getName().trim());
            ps.setString(3, resource.getType().name());
            ps.setInt(4, resource.getCapacity());
            ps.setString(5, resource.getBuilding().trim());
            ps.setBoolean(6, resource.isAvailable());
            ps.setInt(7, resource.getId());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM resources WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Resource mapRow(ResultSet rs) throws SQLException {
        return new Resource(
            rs.getInt("id"),
            rs.getString("code"),
            rs.getString("name"),
            Resource.ResourceType.valueOf(rs.getString("type")),
            rs.getInt("capacity"),
            rs.getString("building"),
            rs.getBoolean("is_available")
        );
    }
}
