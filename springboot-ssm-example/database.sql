-- 创建数据库
CREATE DATABASE IF NOT EXISTS testdb;
USE testdb;

-- 创建用户表（用于登录）
CREATE TABLE IF NOT EXISTS user(
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(20) NOT NULL,
    age INT,
    password VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(80),
    building VARCHAR(50),
    room VARCHAR(50)
);

-- 创建学生表（用于学生信息管理）
CREATE TABLE IF NOT EXISTS student(
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    age INT NOT NULL,
    gender VARCHAR(10)
);

-- 插入用户测试数据（用于登录）
INSERT INTO user(name, age, password, phone, email, building, room) VALUES
('张三', 20, '123456', '13800000001', 'zhangsan@example.com', '1号楼', '101'),
('李四', 22, '654321', '13800000002', 'lisi@example.com', '2号楼', '202');

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
