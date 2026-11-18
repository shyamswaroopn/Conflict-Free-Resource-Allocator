package com.allocator.dao;

import com.allocator.config.DatabaseConnection;
import com.allocator.model.Resource;
import com.allocator.model.Task;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for Tasks/Exams and their Student Enrollments via MySQL JDBC.
 * Uses transactional batch operations when saving student enrollments.
 */
public class TaskDAO {

    public List<Task> findAllWithEnrollments() throws SQLException {
        List<Task> tasks = new ArrayList<>();
        Map<Integer, Task> taskMap = new HashMap<>();

        String taskSql = "SELECT id, code, title, department, duration_mins, required_resource_type, instructor FROM tasks ORDER BY code ASC";
        String enrollSql = "SELECT task_id, student_id FROM task_enrollments";

        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(taskSql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Task t = new Task(
                        rs.getInt("id"),
                        rs.getString("code"),
                        rs.getString("title"),
                        rs.getString("department"),
                        rs.getInt("duration_mins"),
                        Resource.ResourceType.valueOf(rs.getString("required_resource_type")),
                        rs.getString("instructor")
                    );
                    tasks.add(t);
                    taskMap.put(t.getId(), t);
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(enrollSql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int taskId = rs.getInt("task_id");
                    int studentId = rs.getInt("student_id");
                    Task task = taskMap.get(taskId);
                    if (task != null) {
                        task.addStudentId(studentId);
                    }
                }
            }
        }
        return tasks;
    }

    public Task insertWithEnrollments(Task task) throws SQLException {
        String insertTaskSql = "INSERT INTO tasks (code, title, department, duration_mins, required_resource_type, instructor) VALUES (?, ?, ?, ?, ?, ?)";
        String insertEnrollSql = "INSERT INTO task_enrollments (task_id, student_id) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(insertTaskSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, task.getCode().trim());
                    ps.setString(2, task.getTitle().trim());
                    ps.setString(3, task.getDepartment().trim());
                    ps.setInt(4, task.getDurationMins());
                    ps.setString(5, task.getRequiredResourceType().name());
                    ps.setString(6, task.getInstructor().trim());
                    ps.executeUpdate();

                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) {
                            task.setId(keys.getInt(1));
                        }
                    }
                }

                if (!task.getEnrolledStudentIds().isEmpty()) {
                    try (PreparedStatement psEnroll = conn.prepareStatement(insertEnrollSql)) {
                        for (Integer studentId : task.getEnrolledStudentIds()) {
                            psEnroll.setInt(1, task.getId());
                            psEnroll.setInt(2, studentId);
                            psEnroll.addBatch();
                        }
                        psEnroll.executeBatch();
                    }
                }

                conn.commit();
                return task;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public boolean updateWithEnrollments(Task task) throws SQLException {
        String updateTaskSql = "UPDATE tasks SET code = ?, title = ?, department = ?, duration_mins = ?, required_resource_type = ?, instructor = ? WHERE id = ?";
        String deleteEnrollSql = "DELETE FROM task_enrollments WHERE task_id = ?";
        String insertEnrollSql = "INSERT INTO task_enrollments (task_id, student_id) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int updated;
                try (PreparedStatement ps = conn.prepareStatement(updateTaskSql)) {
                    ps.setString(1, task.getCode().trim());
                    ps.setString(2, task.getTitle().trim());
                    ps.setString(3, task.getDepartment().trim());
                    ps.setInt(4, task.getDurationMins());
                    ps.setString(5, task.getRequiredResourceType().name());
                    ps.setString(6, task.getInstructor().trim());
                    ps.setInt(7, task.getId());
                    updated = ps.executeUpdate();
                }

                try (PreparedStatement psDel = conn.prepareStatement(deleteEnrollSql)) {
                    psDel.setInt(1, task.getId());
                    psDel.executeUpdate();
                }

                if (!task.getEnrolledStudentIds().isEmpty()) {
                    try (PreparedStatement psIns = conn.prepareStatement(insertEnrollSql)) {
                        for (Integer studentId : task.getEnrolledStudentIds()) {
                            psIns.setInt(1, task.getId());
                            psIns.setInt(2, studentId);
                            psIns.addBatch();
                        }
                        psIns.executeBatch();
                    }
                }

                conn.commit();
                return updated > 0;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public boolean delete(int taskId) throws SQLException {
        String sql = "DELETE FROM tasks WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, taskId);
            return ps.executeUpdate() > 0;
        }
    }
}
