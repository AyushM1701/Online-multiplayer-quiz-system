CREATE DATABASE quiz_db;

USE quiz_db;

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50)
);

CREATE TABLE questions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    question TEXT,
    option1 VARCHAR(100),
    option2 VARCHAR(100),
    option3 VARCHAR(100),
    option4 VARCHAR(100),
    answer INT
);

CREATE TABLE scores (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50),
    score INT
);