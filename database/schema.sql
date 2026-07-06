-- ============================================
-- 青青社区便民服务平台 - 数据库设计
-- ============================================

CREATE DATABASE IF NOT EXISTS community_platform
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE community_platform;

-- ============================================
-- 1. 用户表
-- ============================================
CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码(BCrypt)',
    real_name VARCHAR(50) COMMENT '真实姓名',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    avatar VARCHAR(500) COMMENT '头像URL',
    
    building VARCHAR(50) COMMENT '楼栋号',
    room VARCHAR(20) COMMENT '房间号',
    role TINYINT NOT NULL DEFAULT 0 COMMENT '角色: 0-普通用户 1-管理员 2-超级管理员 3-维修师傅',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除'
) COMMENT '用户表';

-- ============================================
-- 2. 商品分类表
-- ============================================
CREATE TABLE goods_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    icon VARCHAR(200) COMMENT '分类图标',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '商品分类表';

-- ============================================
-- 3. 二手商品表
-- ============================================
CREATE TABLE second_hand_goods (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '商品ID',
    title VARCHAR(200) NOT NULL COMMENT '商品标题',
    description TEXT COMMENT '商品描述',
    category_id BIGINT COMMENT '分类ID',
    original_price DECIMAL(10,2) COMMENT '原价',
    selling_price DECIMAL(10,2) NOT NULL COMMENT '售价',
    ai_estimated_price DECIMAL(10,2) COMMENT 'AI估价',
    condition_level VARCHAR(20) COMMENT '成色: 全新/九成新/八成新/七成新/六成新以下',
    images TEXT COMMENT '商品图片(Base64或URL)',
    seller_id BIGINT NOT NULL COMMENT '卖家ID',
    buyer_id BIGINT COMMENT '买家ID',
    status TINYINT DEFAULT 0 COMMENT '状态: 0-待审核 1-在售 2-已售 3-下架 4-审核不通过',
    view_count INT DEFAULT 0 COMMENT '浏览次数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    FOREIGN KEY (category_id) REFERENCES goods_category(id),
    FOREIGN KEY (seller_id) REFERENCES sys_user(id),
    FOREIGN KEY (buyer_id) REFERENCES sys_user(id)
) COMMENT '二手商品表';

-- ============================================
-- 4. 商品订单表
-- ============================================
CREATE TABLE goods_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
    order_no VARCHAR(32) NOT NULL UNIQUE COMMENT '订单编号',
    goods_id BIGINT NOT NULL COMMENT '商品ID',
    buyer_id BIGINT NOT NULL COMMENT '买家ID',
    seller_id BIGINT NOT NULL COMMENT '卖家ID',
    amount DECIMAL(10,2) NOT NULL COMMENT '交易金额',
    status TINYINT DEFAULT 0 COMMENT '状态: 0-待确认 1-已确认 2-已完成 3-已取消',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (goods_id) REFERENCES second_hand_goods(id),
    FOREIGN KEY (buyer_id) REFERENCES sys_user(id),
    FOREIGN KEY (seller_id) REFERENCES sys_user(id)
) COMMENT '商品订单表';

-- ============================================
-- 5. 检修需求表
-- ============================================
CREATE TABLE repair_request (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '需求ID',
    user_id BIGINT NOT NULL COMMENT '发布者ID',
    title VARCHAR(200) NOT NULL COMMENT '需求标题',
    description TEXT COMMENT '需求描述',
    repair_type VARCHAR(50) COMMENT '检修类型: 水电维修/家电维修/管道疏通/其他',
    location VARCHAR(200) COMMENT '维修地址',
    urgency TINYINT DEFAULT 2 COMMENT '紧急程度: 1-紧急 2-一般 3-不急',
    ai_tags VARCHAR(500) COMMENT 'AI提取的标签(JSON)',
    ai_urgency VARCHAR(20) COMMENT 'AI判断的紧急程度',
    images VARCHAR(2000) COMMENT '图片(JSON数组)',
    status TINYINT DEFAULT 0 COMMENT '状态: 0-待接单 1-已接单 2-维修中 3-已完成 4-已取消',
    worker_id BIGINT COMMENT '接单师傅ID',
    estimated_price DECIMAL(10,2) COMMENT '预估价格',
    actual_price DECIMAL(10,2) COMMENT '实际价格',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES sys_user(id),
    FOREIGN KEY (worker_id) REFERENCES sys_user(id)
) COMMENT '检修需求表';

-- ============================================
-- 6. 检修订单表
-- ============================================
CREATE TABLE repair_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
    order_no VARCHAR(32) NOT NULL UNIQUE COMMENT '订单编号',
    request_id BIGINT NOT NULL COMMENT '需求ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    worker_id BIGINT NOT NULL COMMENT '师傅ID',
    amount DECIMAL(10,2) COMMENT '维修费用',
    status TINYINT DEFAULT 0 COMMENT '状态: 0-进行中 1-已完成 2-已评价',
    rating TINYINT COMMENT '评价: 1-5星',
    comment TEXT COMMENT '评价内容',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (request_id) REFERENCES repair_request(id),
    FOREIGN KEY (user_id) REFERENCES sys_user(id),
    FOREIGN KEY (worker_id) REFERENCES sys_user(id)
) COMMENT '检修订单表';

-- ============================================
-- 7. 跑腿需求表
-- ============================================
CREATE TABLE errand_request (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '需求ID',
    user_id BIGINT NOT NULL COMMENT '发布者ID',
    title VARCHAR(200) NOT NULL COMMENT '需求标题',
    description TEXT COMMENT '需求描述',
    errand_type VARCHAR(50) COMMENT '跑腿类型: 代买/代取/代送/排队/其他',
    pickup_address VARCHAR(200) COMMENT '取货地址',
    delivery_address VARCHAR(200) COMMENT '送达地址',
    urgency TINYINT DEFAULT 2 COMMENT '紧急程度: 1-紧急 2-一般 3-不急',
    reward DECIMAL(10,2) DEFAULT 0 COMMENT '报酬',
    ai_tags VARCHAR(500) COMMENT 'AI提取的标签(JSON)',
    ai_urgency VARCHAR(20) COMMENT 'AI判断的紧急程度',
    status TINYINT DEFAULT 0 COMMENT '状态: 0-待接单 1-已接单 2-进行中 3-已完成 4-已取消',
    runner_id BIGINT COMMENT '接单人ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES sys_user(id),
    FOREIGN KEY (runner_id) REFERENCES sys_user(id)
) COMMENT '跑腿需求表';

-- ============================================
-- 8. 跑腿订单表
-- ============================================
CREATE TABLE errand_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
    order_no VARCHAR(32) NOT NULL UNIQUE COMMENT '订单编号',
    request_id BIGINT NOT NULL COMMENT '需求ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    runner_id BIGINT NOT NULL COMMENT '跑腿人ID',
    amount DECIMAL(10,2) COMMENT '跑腿费用',
    status TINYINT DEFAULT 0 COMMENT '状态: 0-进行中 1-已完成 2-已评价',
    rating TINYINT COMMENT '评价: 1-5星',
    comment TEXT COMMENT '评价内容',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (request_id) REFERENCES errand_request(id),
    FOREIGN KEY (user_id) REFERENCES sys_user(id),
    FOREIGN KEY (runner_id) REFERENCES sys_user(id)
) COMMENT '跑腿订单表';

-- ====================-- ============================================
-- -- 青青社区便民服务平台 - 数据库设计
-- -- ============================================
--
-- CREATE DATABASE IF NOT EXISTS community_platform
--     DEFAULT CHARACTER SET utf8mb4
--     DEFAULT COLLATE utf8mb4_unicode_ci;
--
-- USE community_platform;
--
-- -- ============================================
-- -- 1. 用户表
-- -- ============================================
-- CREATE TABLE sys_user (
--     id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
--     username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
--     password VARCHAR(255) NOT NULL COMMENT '密码(BCrypt)',
--     real_name VARCHAR(50) COMMENT '真实姓名',
--     phone VARCHAR(20) COMMENT '手机号',
--     email VARCHAR(100) COMMENT '邮箱',
--     avatar VARCHAR(500) COMMENT '头像URL',
--
--     building VARCHAR(50) COMMENT '楼栋号',
--     room VARCHAR(20) COMMENT '房间号',
--     role TINYINT NOT NULL DEFAULT 0 COMMENT '角色: 0-普通用户 1-管理员 2-超级管理员 3-维修师傅',
--     status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
--     created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
--     updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
--     deleted TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除'
-- ) COMMENT '用户表';
--
-- -- ============================================
-- -- 2. 商品分类表
-- -- ============================================
-- CREATE TABLE goods_category (
--     id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
--     name VARCHAR(50) NOT NULL COMMENT '分类名称',
--     icon VARCHAR(200) COMMENT '分类图标',
--     sort_order INT DEFAULT 0 COMMENT '排序',
--     status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
--     created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
--     updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
-- ) COMMENT '商品分类表';
--
-- -- ============================================
-- -- 3. 二手商品表
-- -- ============================================
-- CREATE TABLE second_hand_goods (
--     id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '商品ID',
--     title VARCHAR(200) NOT NULL COMMENT '商品标题',
--     description TEXT COMMENT '商品描述',
--     category_id BIGINT COMMENT '分类ID',
--     original_price DECIMAL(10,2) COMMENT '原价',
--     selling_price DECIMAL(10,2) NOT NULL COMMENT '售价',
--     ai_estimated_price DECIMAL(10,2) COMMENT 'AI估价',
--     condition_level VARCHAR(20) COMMENT '成色: 全新/九成新/八成新/七成新/六成新以下',
--     images TEXT COMMENT '商品图片(Base64或URL)',
--     seller_id BIGINT NOT NULL COMMENT '卖家ID',
--     buyer_id BIGINT COMMENT '买家ID',
--     status TINYINT DEFAULT 0 COMMENT '状态: 0-待审核 1-在售 2-已售 3-下架 4-审核不通过',
--     view_count INT DEFAULT 0 COMMENT '浏览次数',
--     created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
--     updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
--     deleted TINYINT DEFAULT 0,
--     FOREIGN KEY (category_id) REFERENCES goods_category(id),
--     FOREIGN KEY (seller_id) REFERENCES sys_user(id),
--     FOREIGN KEY (buyer_id) REFERENCES sys_user(id)
-- ) COMMENT '二手商品表';
--
-- -- ============================================
-- -- 4. 商品订单表
-- -- ============================================
-- CREATE TABLE goods_order (
--     id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
--     order_no VARCHAR(32) NOT NULL UNIQUE COMMENT '订单编号',
--     goods_id BIGINT NOT NULL COMMENT '商品ID',
--     buyer_id BIGINT NOT NULL COMMENT '买家ID',
--     seller_id BIGINT NOT NULL COMMENT '卖家ID',
--     amount DECIMAL(10,2) NOT NULL COMMENT '交易金额',
--     status TINYINT DEFAULT 0 COMMENT '状态: 0-待确认 1-已确认 2-已完成 3-已取消',
--     created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
--     updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
--     FOREIGN KEY (goods_id) REFERENCES second_hand_goods(id),
--     FOREIGN KEY (buyer_id) REFERENCES sys_user(id),
--     FOREIGN KEY (seller_id) REFERENCES sys_user(id)
-- ) COMMENT '商品订单表';
--
-- -- ============================================
-- -- 5. 检修需求表
-- -- ============================================
-- CREATE TABLE repair_request (
--     id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '需求ID',
--     user_id BIGINT NOT NULL COMMENT '发布者ID',
--     title VARCHAR(200) NOT NULL COMMENT '需求标题',
--     description TEXT COMMENT '需求描述',
--     repair_type VARCHAR(50) COMMENT '检修类型: 水电维修/家电维修/管道疏通/其他',
--     location VARCHAR(200) COMMENT '维修地址',
--     urgency TINYINT DEFAULT 2 COMMENT '紧急程度: 1-紧急 2-一般 3-不急',
--     ai_tags VARCHAR(500) COMMENT 'AI提取的标签(JSON)',
--     ai_urgency VARCHAR(20) COMMENT 'AI判断的紧急程度',
--     images VARCHAR(2000) COMMENT '图片(JSON数组)',
--     status TINYINT DEFAULT 0 COMMENT '状态: 0-待接单 1-已接单 2-维修中 3-已完成 4-已取消',
--     worker_id BIGINT COMMENT '接单师傅ID',
--     estimated_price DECIMAL(10,2) COMMENT '预估价格',
--     actual_price DECIMAL(10,2) COMMENT '实际价格',
--     created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
--     updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
--     deleted TINYINT DEFAULT 0,
--     FOREIGN KEY (user_id) REFERENCES sys_user(id),
--     FOREIGN KEY (worker_id) REFERENCES sys_user(id)
-- ) COMMENT '检修需求表';
--
-- -- ============================================
-- -- 6. 检修订单表
-- -- ============================================
-- CREATE TABLE repair_order (
--     id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
--     order_no VARCHAR(32) NOT NULL UNIQUE COMMENT '订单编号',
--     request_id BIGINT NOT NULL COMMENT '需求ID',
--     user_id BIGINT NOT NULL COMMENT '用户ID',
--     worker_id BIGINT NOT NULL COMMENT '师傅ID',
--     amount DECIMAL(10,2) COMMENT '维修费用',
--     status TINYINT DEFAULT 0 COMMENT '状态: 0-进行中 1-已完成 2-已评价',
--     rating TINYINT COMMENT '评价: 1-5星',
--     comment TEXT COMMENT '评价内容',
--     created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
--     updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
--     FOREIGN KEY (request_id) REFERENCES repair_request(id),
--     FOREIGN KEY (user_id) REFERENCES sys_user(id),
--     FOREIGN KEY (worker_id) REFERENCES sys_user(id)
-- ) COMMENT '检修订单表';
--
-- -- ============================================
-- -- 7. 跑腿需求表
-- -- ============================================
-- CREATE TABLE errand_request (
--     id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '需求ID',
--     user_id BIGINT NOT NULL COMMENT '发布者ID',
--     title VARCHAR(200) NOT NULL COMMENT '需求标题',
--     description TEXT COMMENT '需求描述',
--     errand_type VARCHAR(50) COMMENT '跑腿类型: 代买/代取/代送/排队/其他',
--     pickup_address VARCHAR(200) COMMENT '取货地址',
--     delivery_address VARCHAR(200) COMMENT '送达地址',
--     urgency TINYINT DEFAULT 2 COMMENT '紧急程度: 1-紧急 2-一般 3-不急',
--     reward DECIMAL(10,2) DEFAULT 0 COMMENT '报酬',
--     ai_tags VARCHAR(500) COMMENT 'AI提取的标签(JSON)',
--     ai_urgency VARCHAR(20) COMMENT 'AI判断的紧急程度',
--     status TINYINT DEFAULT 0 COMMENT '状态: 0-待接单 1-已接单 2-进行中 3-已完成 4-已取消',
--     runner_id BIGINT COMMENT '接单人ID',
--     created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
--     updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
--     deleted TINYINT DEFAULT 0,
--     FOREIGN KEY (user_id) REFERENCES sys_user(id),
--     FOREIGN KEY (runner_id) REFERENCES sys_user(id)
-- ) COMMENT '跑腿需求表';
--
-- -- ============================================
-- -- 8. 跑腿订单表
-- -- ============================================
-- CREATE TABLE errand_order (
--     id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
--     order_no VARCHAR(32) NOT NULL UNIQUE COMMENT '订单编号',
--     request_id BIGINT NOT NULL COMMENT '需求ID',
--     user_id BIGINT NOT NULL COMMENT '用户ID',
--     runner_id BIGINT NOT NULL COMMENT '跑腿人ID',
--     amount DECIMAL(10,2) COMMENT '跑腿费用',
--     status TINYINT DEFAULT 0 COMMENT '状态: 0-进行中 1-已完成 2-已评价',
--     rating TINYINT COMMENT '评价: 1-5星',
--     comment TEXT COMMENT '评价内容',
--     created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
--     updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
--     FOREIGN KEY (request_id) REFERENCES errand_request(id),
--     FOREIGN KEY (user_id) REFERENCES sys_user(id),
--     FOREIGN KEY (runner_id) REFERENCES sys_user(id)
-- ) COMMENT '跑腿订单表';
--
-- -- ============================================
-- -- 9. 论坛帖子表
-- -- ============================================
-- CREATE TABLE forum_post (
--     id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '帖子ID',
--     user_id BIGINT NOT NULL COMMENT '发布者ID',
--     title VARCHAR(200) NOT NULL COMMENT '帖子标题',
--     content TEXT NOT NULL COMMENT '帖子内容',
--     images VARCHAR(2000) COMMENT '图片(JSON数组)',
--     view_count INT DEFAULT 0 COMMENT '浏览数',
--     like_count INT DEFAULT 0 COMMENT '点赞数',
--     comment_count INT DEFAULT 0 COMMENT '评论数',
--     status TINYINT DEFAULT 1 COMMENT '状态: 0-审核中 1-已发布 2-已下架',
--     created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
--     updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
--     deleted TINYINT DEFAULT 0,
--     FOREIGN KEY (user_id) REFERENCES sys_user(id)
-- ) COMMENT '论坛帖子表';
--
-- -- ============================================
-- -- 10. 评论表
-- -- ============================================
-- CREATE TABLE forum_comment (
--     id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评论ID',
--     post_id BIGINT NOT NULL COMMENT '帖子ID',
--     user_id BIGINT NOT NULL COMMENT '评论者ID',
--     content TEXT NOT NULL COMMENT '评论内容',
--     parent_id BIGINT COMMENT '父评论ID(用于回复)',
--     like_count INT DEFAULT 0 COMMENT '点赞数',
--     status TINYINT DEFAULT 1 COMMENT '状态: 0-审核中 1-已发布',
--     created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
--     deleted TINYINT DEFAULT 0,
--     FOREIGN KEY (post_id) REFERENCES forum_post(id),
--     FOREIGN KEY (user_id) REFERENCES sys_user(id)
-- ) COMMENT '评论表';
--
-- -- ============================================
-- -- 11. 通知表
-- -- ============================================
-- CREATE TABLE notification (
--     id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '通知ID',
--     user_id BIGINT COMMENT '接收者ID(NULL表示系统通知)',
--     title VARCHAR(200) NOT NULL COMMENT '通知标题',
--     content TEXT COMMENT '通知内容',
--     type TINYINT DEFAULT 1 COMMENT '类型: 1-系统通知 2-订单通知 3-需求通知',
--     is_read TINYINT DEFAULT 0 COMMENT '是否已读: 0-未读 1-已读',
--     created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
--     FOREIGN KEY (user_id) REFERENCES sys_user(id)
-- ) COMMENT '通知表';
--
-- -- ============================================
-- -- 12. AI服务匹配记录表
-- -- ============================================
-- CREATE TABLE ai_match_record (
--     id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
--     request_type VARCHAR(20) NOT NULL COMMENT '需求类型: repair/errand',
--     request_id BIGINT NOT NULL COMMENT '需求ID',
--     raw_description TEXT COMMENT '原始描述',
--     ai_service_type VARCHAR(50) COMMENT 'AI识别的服务类型',
--     ai_urgency VARCHAR(20) COMMENT 'AI识别的紧急程度',
--     ai_skills VARCHAR(500) COMMENT 'AI提取的技能标签(JSON)',
--     matched_workers VARCHAR(500) COMMENT '匹配的师傅ID列表(JSON)',
--     created_at DATETIME DEFAULT CURRENT_TIMESTAMP
-- ) COMMENT 'AI服务匹配记录表';
--
-- -- ============================================
-- -- 13. AI商品估价记录表
-- -- ============================================
-- CREATE TABLE ai_price_record (
--     id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
--     goods_id BIGINT NOT NULL COMMENT '商品ID',
--     goods_name VARCHAR(200) COMMENT '商品名称',
--     condition_level VARCHAR(20) COMMENT '商品成色',
--     original_price DECIMAL(10,2) COMMENT '原价',
--     ai_estimated_price DECIMAL(10,2) COMMENT 'AI估价',
--     ai_price_range_min DECIMAL(10,2) COMMENT 'AI估价区间下限',
--     ai_price_range_max DECIMAL(10,2) COMMENT 'AI估价区间上限',
--     ai_description TEXT COMMENT 'AI优化后的描述',
--     similar_history VARCHAR(2000) COMMENT '相似历史成交数据(JSON)',
--     created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
--     FOREIGN KEY (goods_id) REFERENCES second_hand_goods(id)
-- ) COMMENT 'AI商品估价记录表';
--
-- -- ============================================
-- -- 初始数据
-- -- ============================================
--
-- -- 默认管理员 (密码: admin123, BCrypt加密)
-- INSERT INTO sys_user (username, password, real_name, role, status) VALUES
-- ('admin', '$2a$10$75bt.TUAAsCVdVDmT6eJUuWaUiP.uKlYDtcm7O6ciSx/a1iYlRJH.', '系统管理员', 2, 1);
--
-- -- 商品分类
-- INSERT INTO goods_category (name, icon, sort_order) VALUES
-- ('母婴用品', 'fa-baby', 1),
-- ('图书教材', 'fa-book', 2),
-- ('家电数码', 'fa-laptop', 3),
-- ('服饰鞋包', 'fa-tshirt', 4),
-- ('家居日用', 'fa-couch', 5),
-- ('运动户外', 'fa-football-ball', 6),
-- ('其他', 'fa-ellipsis-h', 7);
--
-- -- 示例用户 (密码由 DataInitializer 启动时自动设置为 admin123)
-- INSERT INTO sys_user (username, password, real_name, phone, building, room, role, status) VALUES
-- ('zhangsan', NULL, '张女士', '13800138001', '3栋', '502', 0, 1),
-- ('lisi', NULL, '李先生', '13800138002', '5栋', '301', 0, 1),
-- ('wangwu', NULL, '王先生', '13800138003', '8栋', '301', 0, 1),
-- ('zhaoliu', NULL, '赵女士', '13800138004', '12栋', '102', 0, 1);
--
-- -- 示例师傅（专职维修人员，密码由 DataInitializer 启动时自动设置为 admin123）
-- INSERT INTO sys_user (username, password, real_name, phone, role, status) VALUES
-- ('worker1', NULL, '王师傅', '13900139001', 3, 1),
-- ('worker2', NULL, '李师傅', '13900139002', 3, 1),
-- ('worker3', NULL, '张师傅', '13900139003', 3, 1);
--
-- -- 示例二手商品
-- INSERT INTO second_hand_goods (title, description, category_id, original_price, selling_price, condition_level, seller_id, status) VALUES
-- ('儿童自行车', '九成新，适合3-6岁儿童，带辅助轮', 1, 399.00, 180.00, '九成新', 3, 1),
-- ('小学课外书全套', '人教版1-6年级课外读物，共28本', 2, 280.00, 120.00, '八成新', 2, 1),
-- ('小米空气净化器', '使用半年，功能完好，附赠滤芯', 3, 699.00, 350.00, '九成新', 5, 0);
--
-- -- 示例检修需求
-- INSERT INTO repair_request (user_id, title, description, repair_type, location, urgency, status) VALUES
-- (2, '空调不制冷', '客厅格力空调开机后不出冷风，已使用3年', '家电维修', '5栋301室', 2, 3),
-- (4, '水管漏水', '厨房水龙头接口处渗水，需要紧急维修', '水电维修', '12栋102室', 1, 2);
--
-- -- 示例跑腿需求
-- INSERT INTO errand_request (user_id, title, description, errand_type, pickup_address, delivery_address, urgency, reward, status) VALUES
-- (2, '代买药品', '需要在附近药店购买感冒灵和体温计', '代买', '小区附近同仁堂', '5栋301室', 1, 15.00, 0),
-- (5, '代取外卖', '美团外卖，小区门口货架自取', '代取', '小区门口外卖货架', '8栋301室', 2, 10.00, 0),
-- (4, '代取快递', '菜鸟驿站有3个包裹需要取回', '代取', '菜鸟驿站', '12栋102室', 3, 12.00, 2);
--
-- -- 示例论坛帖子
-- INSERT INTO forum_post (user_id, title, content, view_count, like_count, comment_count, status) VALUES
-- (2, '周末社区亲子活动报名', '这周六下午3点在小区花园举办亲子活动，有游戏和小礼品，欢迎带小朋友参加！', 156, 23, 8, 1),
-- (3, '闲置物品交换群', '建了一个闲置物品交换微信群，有需要的邻居可以加我微信拉你进群', 89, 15, 5, 1),
-- (5, '小区停车新规通知', '从下月起，地下停车场将实行新的停车收费标准，请各位业主知悉', 234, 8, 12, 1);
--
-- -- 示例评论
-- INSERT INTO forum_comment (post_id, user_id, content, like_count, status) VALUES
-- (1, 3, '太好了！我家小朋友一定参加！', 3, 1),
-- (1, 4, '请问几点开始？需要提前报名吗？', 1, 1),
-- (1, 2, '下午3点开始，不用提前报名，直接来就行', 2, 1),
-- (2, 4, '我想进群，请加我微信：zhaoliu123', 0, 1),
-- (3, 2, '新收费是怎么样的？能详细说说吗？', 4, 1);
--
-- -- 示例通知
-- INSERT INTO notification (user_id, title, content, type, is_read) VALUES
-- (2, '订单完成', '您的空调维修订单已完成，请确认', 2, 0),
-- (3, '商品审核通过', '您发布的"儿童自行车"已通过审核', 1, 0),
-- (5, '新回复', '您发布的帖子有新评论', 3, 1);========================
-- 9. 论坛帖子表
-- ============================================
CREATE TABLE forum_post (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '帖子ID',
    user_id BIGINT NOT NULL COMMENT '发布者ID',
    title VARCHAR(200) NOT NULL COMMENT '帖子标题',
    content TEXT NOT NULL COMMENT '帖子内容',
    images VARCHAR(2000) COMMENT '图片(JSON数组)',
    view_count INT DEFAULT 0 COMMENT '浏览数',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    comment_count INT DEFAULT 0 COMMENT '评论数',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-审核中 1-已发布 2-已下架',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES sys_user(id)
) COMMENT '论坛帖子表';

-- ============================================
-- 10. 评论表
-- ============================================
CREATE TABLE forum_comment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评论ID',
    post_id BIGINT NOT NULL COMMENT '帖子ID',
    user_id BIGINT NOT NULL COMMENT '评论者ID',
    content TEXT NOT NULL COMMENT '评论内容',
    parent_id BIGINT COMMENT '父评论ID(用于回复)',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-审核中 1-已发布',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    FOREIGN KEY (post_id) REFERENCES forum_post(id),
    FOREIGN KEY (user_id) REFERENCES sys_user(id)
) COMMENT '评论表';

-- ============================================
-- 11. 通知表
-- ============================================
CREATE TABLE notification (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '通知ID',
    user_id BIGINT COMMENT '接收者ID(NULL表示系统通知)',
    title VARCHAR(200) NOT NULL COMMENT '通知标题',
    content TEXT COMMENT '通知内容',
    type TINYINT DEFAULT 1 COMMENT '类型: 1-系统通知 2-订单通知 3-需求通知',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读: 0-未读 1-已读',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES sys_user(id)
) COMMENT '通知表';

-- ============================================
-- 12. AI服务匹配记录表
-- ============================================
CREATE TABLE ai_match_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    request_type VARCHAR(20) NOT NULL COMMENT '需求类型: repair/errand',
    request_id BIGINT NOT NULL COMMENT '需求ID',
    raw_description TEXT COMMENT '原始描述',
    ai_service_type VARCHAR(50) COMMENT 'AI识别的服务类型',
    ai_urgency VARCHAR(20) COMMENT 'AI识别的紧急程度',
    ai_skills VARCHAR(500) COMMENT 'AI提取的技能标签(JSON)',
    matched_workers VARCHAR(500) COMMENT '匹配的师傅ID列表(JSON)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT 'AI服务匹配记录表';

-- ============================================
-- 13. AI商品估价记录表
-- ============================================
CREATE TABLE ai_price_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    goods_id BIGINT NOT NULL COMMENT '商品ID',
    goods_name VARCHAR(200) COMMENT '商品名称',
    condition_level VARCHAR(20) COMMENT '商品成色',
    original_price DECIMAL(10,2) COMMENT '原价',
    ai_estimated_price DECIMAL(10,2) COMMENT 'AI估价',
    ai_price_range_min DECIMAL(10,2) COMMENT 'AI估价区间下限',
    ai_price_range_max DECIMAL(10,2) COMMENT 'AI估价区间上限',
    ai_description TEXT COMMENT 'AI优化后的描述',
    similar_history VARCHAR(2000) COMMENT '相似历史成交数据(JSON)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (goods_id) REFERENCES second_hand_goods(id)
) COMMENT 'AI商品估价记录表';

-- ============================================
-- 初始数据
-- ============================================

-- 默认管理员 (密码: admin123, BCrypt加密)
INSERT INTO sys_user (username, password, real_name, role, status) VALUES
('admin', '$2a$10$75bt.TUAAsCVdVDmT6eJUuWaUiP.uKlYDtcm7O6ciSx/a1iYlRJH.', '系统管理员', 2, 1);

-- 商品分类
INSERT INTO goods_category (name, icon, sort_order) VALUES
('母婴用品', 'fa-baby', 1),
('图书教材', 'fa-book', 2),
('家电数码', 'fa-laptop', 3),
('服饰鞋包', 'fa-tshirt', 4),
('家居日用', 'fa-couch', 5),
('运动户外', 'fa-football-ball', 6),
('其他', 'fa-ellipsis-h', 7);

-- 示例用户 (密码由 DataInitializer 启动时自动设置为 admin123)
INSERT INTO sys_user (username, password, real_name, phone, building, room, role, status) VALUES
('zhangsan', NULL, '张女士', '13800138001', '3栋', '502', 0, 1),
('lisi', NULL, '李先生', '13800138002', '5栋', '301', 0, 1),
('wangwu', NULL, '王先生', '13800138003', '8栋', '301', 0, 1),
('zhaoliu', NULL, '赵女士', '13800138004', '12栋', '102', 0, 1);

-- 示例师傅（专职维修人员，密码由 DataInitializer 启动时自动设置为 admin123）
INSERT INTO sys_user (username, password, real_name, phone, role, status) VALUES
('worker1', NULL, '王师傅', '13900139001', 3, 1),
('worker2', NULL, '李师傅', '13900139002', 3, 1),
('worker3', NULL, '张师傅', '13900139003', 3, 1);

-- 示例二手商品
INSERT INTO second_hand_goods (title, description, category_id, original_price, selling_price, condition_level, seller_id, status) VALUES
('儿童自行车', '九成新，适合3-6岁儿童，带辅助轮', 1, 399.00, 180.00, '九成新', 3, 1),
('小学课外书全套', '人教版1-6年级课外读物，共28本', 2, 280.00, 120.00, '八成新', 2, 1),
('小米空气净化器', '使用半年，功能完好，附赠滤芯', 3, 699.00, 350.00, '九成新', 5, 0);

-- 示例检修需求
INSERT INTO repair_request (user_id, title, description, repair_type, location, urgency, status) VALUES
(2, '空调不制冷', '客厅格力空调开机后不出冷风，已使用3年', '家电维修', '5栋301室', 2, 3),
(4, '水管漏水', '厨房水龙头接口处渗水，需要紧急维修', '水电维修', '12栋102室', 1, 2);

-- 示例跑腿需求
INSERT INTO errand_request (user_id, title, description, errand_type, pickup_address, delivery_address, urgency, reward, status) VALUES
(2, '代买药品', '需要在附近药店购买感冒灵和体温计', '代买', '小区附近同仁堂', '5栋301室', 1, 15.00, 0),
(5, '代取外卖', '美团外卖，小区门口货架自取', '代取', '小区门口外卖货架', '8栋301室', 2, 10.00, 0),
(4, '代取快递', '菜鸟驿站有3个包裹需要取回', '代取', '菜鸟驿站', '12栋102室', 3, 12.00, 2);

-- 示例论坛帖子
INSERT INTO forum_post (user_id, title, content, view_count, like_count, comment_count, status) VALUES
(2, '周末社区亲子活动报名', '这周六下午3点在小区花园举办亲子活动，有游戏和小礼品，欢迎带小朋友参加！', 156, 23, 8, 1),
(3, '闲置物品交换群', '建了一个闲置物品交换微信群，有需要的邻居可以加我微信拉你进群', 89, 15, 5, 1),
(5, '小区停车新规通知', '从下月起，地下停车场将实行新的停车收费标准，请各位业主知悉', 234, 8, 12, 1);

-- 示例评论
INSERT INTO forum_comment (post_id, user_id, content, like_count, status) VALUES
(1, 3, '太好了！我家小朋友一定参加！', 3, 1),
(1, 4, '请问几点开始？需要提前报名吗？', 1, 1),
(1, 2, '下午3点开始，不用提前报名，直接来就行', 2, 1),
(2, 4, '我想进群，请加我微信：zhaoliu123', 0, 1),
(3, 2, '新收费是怎么样的？能详细说说吗？', 4, 1);

-- 示例通知
INSERT INTO notification (user_id, title, content, type, is_read) VALUES
(2, '订单完成', '您的空调维修订单已完成，请确认', 2, 0),
(3, '商品审核通过', '您发布的"儿童自行车"已通过审核', 1, 0
),
(5, '新回复', '您发布的帖子有新评论', 3, 1);
