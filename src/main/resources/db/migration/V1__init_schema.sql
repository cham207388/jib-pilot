-- Migration script to create initial database schema
-- This script creates tables, indexes, and relationships for the Jib Pilot application

-- Create students table
CREATE TABLE students (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    CONSTRAINT uk_students_email UNIQUE (email)
);

-- Create courses table
CREATE TABLE courses (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    CONSTRAINT uk_courses_code UNIQUE (code)
);

-- Create users table (UserAccount entity)
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    student_id BIGINT UNIQUE,
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT fk_users_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT ck_users_role CHECK (role IN ('STUDENT', 'ADMIN'))
);

-- Create student_courses join table (Many-to-Many relationship)
CREATE TABLE student_courses (
    student_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    PRIMARY KEY (student_id, course_id),
    CONSTRAINT fk_student_courses_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT fk_student_courses_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
);

-- Create indexes for better query performance

-- Index on students.email (already has unique constraint, but explicit index for clarity)
CREATE INDEX idx_students_email ON students(email);

-- Index on courses.code (already has unique constraint, but explicit index for clarity)
CREATE INDEX idx_courses_code ON courses(code);

-- Index on users.email (already has unique constraint, but explicit index for clarity)
CREATE INDEX idx_users_email ON users(email);

-- Index on users.role for filtering by role
CREATE INDEX idx_users_role ON users(role);

-- Index on users.student_id (foreign key, commonly queried)
CREATE INDEX idx_users_student_id ON users(student_id);

-- Indexes on student_courses join table for efficient lookups
CREATE INDEX idx_student_courses_student_id ON student_courses(student_id);
CREATE INDEX idx_student_courses_course_id ON student_courses(course_id);

