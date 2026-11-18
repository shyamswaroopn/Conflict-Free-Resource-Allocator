-- =============================================================================
-- Conflict-Free Resource Allocation System Using Graph Coloring (Java PBL)
-- MySQL Database Schema & Initial University Dataset
-- Compatible with MySQL 8.0+ / MariaDB 10.5+
-- =============================================================================

CREATE DATABASE IF NOT EXISTS conflict_free_allocator
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE conflict_free_allocator;

-- Drop existing tables in reverse dependency order for clean initialization
DROP TABLE IF EXISTS allocations;
DROP TABLE IF EXISTS task_enrollments;
DROP TABLE IF EXISTS tasks;
DROP TABLE IF EXISTS resources;
DROP TABLE IF EXISTS students;

-- 1. Students Table
CREATE TABLE students (
    id INT AUTO_INCREMENT PRIMARY KEY,
    roll_no VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    department VARCHAR(60) NOT NULL,
    semester INT NOT NULL CHECK (semester BETWEEN 1 AND 8),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 2. Resources Table (Classrooms, Laboratories, Auditoriums)
CREATE TABLE resources (
    id INT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(25) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    type ENUM('CLASSROOM', 'LAB', 'AUDITORIUM') NOT NULL,
    capacity INT NOT NULL CHECK (capacity > 0),
    building VARCHAR(80) NOT NULL,
    is_available BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 3. Tasks / Exams Table (Each row represents a Vertex V in the Conflict Graph)
CREATE TABLE tasks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(25) NOT NULL UNIQUE,
    title VARCHAR(150) NOT NULL,
    department VARCHAR(60) NOT NULL,
    duration_mins INT NOT NULL DEFAULT 180 CHECK (duration_mins > 0),
    required_resource_type ENUM('CLASSROOM', 'LAB', 'AUDITORIUM') NOT NULL DEFAULT 'CLASSROOM',
    instructor VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 4. Task Enrollments Table (Defines Student-Task mappings; shared students induce Graph Edges)
CREATE TABLE task_enrollments (
    task_id INT NOT NULL,
    student_id INT NOT NULL,
    PRIMARY KEY (task_id, student_id),
    CONSTRAINT fk_enrollment_task FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    CONSTRAINT fk_enrollment_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 5. Allocations Table (Stores Graph Coloring Results: Color -> Time Slot + Resource)
CREATE TABLE allocations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    task_id INT NOT NULL UNIQUE,
    color_index INT NOT NULL CHECK (color_index >= 0),
    time_slot_label VARCHAR(100) NOT NULL,
    resource_id INT NOT NULL,
    algorithm_used VARCHAR(40) NOT NULL DEFAULT 'WELSH_POWELL',
    is_manual_override BOOLEAN NOT NULL DEFAULT FALSE,
    allocated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_alloc_task FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    CONSTRAINT fk_alloc_resource FOREIGN KEY (resource_id) REFERENCES resources(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- =============================================================================
-- REALISTIC UNIVERSITY SEED DATA FOR TESTING & DEMONSTRATION
-- =============================================================================

INSERT INTO students (roll_no, name, email, department, semester) VALUES
('CS2024001', 'Aarav Sharma', 'aarav.sharma@univ.edu', 'Computer Science', 5),
('CS2024002', 'Diya Patel', 'diya.patel@univ.edu', 'Computer Science', 5),
('CS2024003', 'Rohan Verma', 'rohan.verma@univ.edu', 'Computer Science', 5),
('CS2024004', 'Ananya Nair', 'ananya.nair@univ.edu', 'Computer Science', 5),
('EC2024005', 'Kabir Mehta', 'kabir.mehta@univ.edu', 'Electronics', 5),
('EC2024006', 'Meera Krishnan', 'meera.krishnan@univ.edu', 'Electronics', 5),
('IT2024007', 'Siddharth Rao', 'siddharth.rao@univ.edu', 'Information Tech', 5),
('IT2024008', 'Neha Kulkarni', 'neha.kulkarni@univ.edu', 'Information Tech', 5),
('AIML2024009', 'Vikramaditya Joshi', 'vikram.joshi@univ.edu', 'AI & Data Science', 5),
('AIML2024010', 'Priya Deshmukh', 'priya.deshmukh@univ.edu', 'AI & Data Science', 5);

INSERT INTO resources (code, name, type, capacity, building, is_available) VALUES
('RM-101', 'Lecture Hall 101', 'CLASSROOM', 60, 'Academic Block A', TRUE),
('RM-102', 'Lecture Hall 102', 'CLASSROOM', 50, 'Academic Block A', TRUE),
('RM-201', 'Seminar Room 201', 'CLASSROOM', 45, 'Academic Block B', TRUE),
('LAB-301', 'Advanced Computing Lab 1', 'LAB', 40, 'Tech Tower Floor 3', TRUE),
('LAB-302', 'Systems & Networks Lab 2', 'LAB', 35, 'Tech Tower Floor 3', TRUE),
('AUD-01', 'Main Turing Auditorium', 'AUDITORIUM', 180, 'Central Convention Wing', TRUE);

INSERT INTO tasks (code, title, department, duration_mins, required_resource_type, instructor) VALUES
('CS301', 'Design & Analysis of Algorithms', 'Computer Science', 180, 'CLASSROOM', 'Dr. S. Ramanujan'),
('CS302', 'Operating Systems & Internals', 'Computer Science', 180, 'LAB', 'Prof. M. Subramanian'),
('CS303', 'Database Management Systems', 'Computer Science', 180, 'LAB', 'Dr. A. Deshpande'),
('MA304', 'Discrete Mathematics & Graph Theory', 'Mathematics', 180, 'CLASSROOM', 'Dr. K. Viswanathan'),
('EC305', 'Microprocessors & Embedded Systems', 'Electronics', 180, 'LAB', 'Prof. R. Gopalan'),
('AI306', 'Machine Learning Foundations', 'AI & Data Science', 180, 'CLASSROOM', 'Dr. N. Chatterjee'),
('CS307', 'Computer Networks & Security', 'Computer Science', 180, 'CLASSROOM', 'Prof. M. Subramanian'),
('HU308', 'Engineering Economics & Ethics', 'Humanities', 120, 'AUDITORIUM', 'Dr. P. Sengupta');

-- Student Enrollments (Creating rich conflict edges across courses)
-- CS301 & CS302 & CS303 & MA304 share core CS students
INSERT INTO task_enrollments (task_id, student_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 7), (1, 9),
(2, 1), (2, 2), (2, 3), (2, 7),
(3, 2), (3, 3), (3, 4), (3, 8), (3, 10),
(4, 1), (4, 4), (4, 5), (4, 6), (4, 9),
(5, 5), (5, 6), (5, 7),
(6, 1), (6, 4), (6, 9), (6, 10),
(7, 3), (7, 6), (7, 7), (7, 8),
(8, 2), (8, 5), (8, 8), (8, 10);
