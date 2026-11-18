package com.allocator.dao;

import com.allocator.config.DatabaseConnection;
import com.allocator.model.Student;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Student records using strict MySQL JDBC and PreparedStatements.
 */
public class StudentDAO {

    public List<Student> findAll() throws SQLException {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT id, roll_no, name, email, department, semester FROM students ORDER BY roll_no ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                students.add(mapRow(rs));
            }
        }
        return students;
    }

    public Student findById(int id) throws SQLException {
        String sql = "SELECT id, roll_no, name, email, department, semester FROM students WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public Student insert(Student student) throws SQLException {
        String sql = "INSERT INTO students (roll_no, name, email, department, semester) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, student.getRollNo().trim());
            ps.setString(2, student.getName().trim());
            ps.setString(3, student.getEmail().trim());
            ps.setString(4, student.getDepartment().trim());
            ps.setInt(5, student.getSemester());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Inserting student failed, no rows affected.");
            }
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    student.setId(keys.getInt(1));
                }
            }
        }
        return student;
    }

    public boolean update(Student student) throws SQLException {
        String sql = "UPDATE students SET roll_no = ?, name = ?, email = ?, department = ?, semester = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, student.getRollNo().trim());
            ps.setString(2, student.getName().trim());
            ps.setString(3, student.getEmail().trim());
            ps.setString(4, student.getDepartment().trim());
            ps.setInt(5, student.getSemester());
            ps.setInt(6, student.getId());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM students WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Student mapRow(ResultSet rs) throws SQLException {
        return new Student(
            rs.getInt("id"),
            rs.getString("roll_no"),
            rs.getString("name"),
            rs.getString("email"),
            rs.getString("department"),
            rs.getInt("semester")
        );
    }
}
