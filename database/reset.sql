-- ============================================================
--  reset.sql – Recreates the quiz_db from scratch
--  Run with: mysql -u root -p < database/reset.sql
-- ============================================================

DROP DATABASE IF EXISTS quiz_db;
CREATE DATABASE quiz_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE quiz_db;

-- ── Questions ────────────────────────────────────────────────────────────────
-- answer column: 0 = option1, 1 = option2, 2 = option3, 3 = option4
CREATE TABLE questions (
    id      INT AUTO_INCREMENT PRIMARY KEY,
    question TEXT         NOT NULL,
    option1  VARCHAR(200) NOT NULL,
    option2  VARCHAR(200) NOT NULL,
    option3  VARCHAR(200) NOT NULL,
    option4  VARCHAR(200) NOT NULL,
    answer   INT          NOT NULL  -- 0-indexed
);

-- ── Scores ───────────────────────────────────────────────────────────────────
CREATE TABLE scores (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    username   VARCHAR(50) NOT NULL,
    score      INT         NOT NULL DEFAULT 0,
    played_at  TIMESTAMP   DEFAULT CURRENT_TIMESTAMP
);

-- ── Seed questions ────────────────────────────────────────────────────────────
INSERT INTO questions (question, option1, option2, option3, option4, answer) VALUES
-- Java basics
('What is Java?',
    'A programming language', 'A wild animal', 'A brand of car', 'A video game', 0),

('OOP stands for?',
    'Object Oriented Programming', 'Only One Process', 'Open Office Program', 'Optimized Output Pipeline', 0),

('Which keyword is used for inheritance in Java?',
    'extends', 'import', 'package', 'class', 0),

('Which data type stores whole numbers in Java?',
    'int', 'float', 'char', 'boolean', 0),

('What does JVM stand for?',
    'Java Virtual Machine', 'Java Verified Module', 'Java Variable Manager', 'Just Variable Method', 0),

('Which method is the entry point of a Java program?',
    'main()', 'start()', 'run()', 'init()', 0),

('Which collection class is synchronized by default in Java?',
    'Vector', 'ArrayList', 'HashMap', 'LinkedList', 0),

('What is the default value of an int in Java?',
    '0', 'null', '1', 'undefined', 0),

('Which operator is used for string concatenation in Java?',
    '+', '&', '||', '%', 0),

('What does SQL stand for?',
    'Structured Query Language', 'Simple Query Logic', 'Server Query Layer', 'Secure Query List', 0);