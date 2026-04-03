DROP DATABASE IF EXISTS quiz_db;

CREATE DATABASE quiz_db;
USE quiz_db;

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

INSERT INTO questions (question, option1, option2, option3, option4, answer)
VALUES
('What is Java?', 'Language', 'Animal', 'Car', 'Game', 0),
('OOP stands for?', 'Object Oriented Programming', 'Only One Process', 'Open Office Program', 'None', 0),
('Which keyword is used for inheritance?', 'extends', 'import', 'package', 'class', 0);