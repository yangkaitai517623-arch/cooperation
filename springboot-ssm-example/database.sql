-- 创建数据库
CREATE DATABASE IF NOT EXISTS testdb;
USE testdb;

-- 创建用户表（用于登录）
CREATE TABLE IF NOT EXISTS user(
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(20) NOT NULL,
    age INT,
    password VARCHAR(50) NOT NULL
);

-- 创建学生表（用于学生信息管理）
CREATE TABLE IF NOT EXISTS student(
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    age INT NOT NULL,
    gender VARCHAR(10)
);

-- 插入用户测试数据（用于登录）
INSERT INTO user(name, age, password) VALUES
('张三', 20, '123456'),
('李四', 22, '654321');

-- 插入学生测试数据（用于学生管理页面展示）
INSERT INTO student(name, age, gender) VALUES
('王小明', 20, '男'),
('李小红', 19, '女'),
('张伟', 21, '男'),
('刘芳', 20, '女'),
('陈强', 22, '男');

-- 验证
SELECT * FROM user;
SELECT * FROM student;
