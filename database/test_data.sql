-- 仪表盘测试数据（补充数据，在 schema.sql 初始数据基础上执行）
-- schema.sql 已有：用户1-8，商品1-3，检修需求1-2，跑腿需求1-3，帖子1-3，评论1-5，通知1-3

USE community_platform;

-- 示例用户 (密码由 DataInitializer 启动时自动设置为 admin123)
INSERT INTO sys_user (username, password, real_name, phone, building, room, role, status) VALUES
('liuyang', NULL, '刘先生', '13800138005', '6栋', '201', 0, 1),
('chenmei', NULL, '陈女士', '13800138006', '9栋', '402', 0, 1),
('huangwei', NULL, '黄先生', '13800138007', '2栋', '101', 0, 1),
('zhoulan', NULL, '周女士', '13800138008', '7栋', '303', 0, 1),
('wugang', NULL, '吴先生', '13800138009', '11栋', '502', 0, 1);

-- 示例二手商品（待审核）
INSERT INTO second_hand_goods (title, description, category_id, original_price, selling_price, condition_level, seller_id, status) VALUES
('iPad Air 4', '自用一年，无磕碰，配件齐全', 3, 4399.00, 2800.00, '九成新', 3, 0),
('婴儿推车', '好孩子品牌，可折叠', 1, 899.00, 350.00, '八成新', 5, 0),
('瑜伽垫', '全新未拆封，加厚款', 6, 129.00, 69.00, '全新', 4, 0);

-- 示例商品订单
INSERT INTO goods_order (order_no, goods_id, buyer_id, seller_id, amount, status) VALUES
('GO20260601001', 1, 4, 3, 180.00, 2),
('GO20260601002', 2, 5, 2, 120.00, 2),
('GO20260601003', 1, 5, 3, 180.00, 1),
('GO20260601004', 2, 4, 2, 120.00, 0);

-- 示例检修需求（待接单）
INSERT INTO repair_request (user_id, title, description, repair_type, location, urgency, status) VALUES
(3, '卫生间漏水', '马桶底部渗水，需要检修', '水电维修', '5栋301室', 1, 0),
(5, '电灯不亮', '客厅吊灯不亮，可能是线路问题', '水电维修', '8栋301室', 2, 0),
(4, '门锁损坏', '大门锁芯转动困难', '其他', '12栋102室', 2, 0);

-- 示例检修订单
INSERT INTO repair_order (order_no, request_id, user_id, worker_id, amount, status, rating, comment) VALUES
('RO20260601001', 1, 2, 6, 280.00, 2, 5, '师傅很专业，修得又快又好'),
('RO20260601002', 2, 4, 7, 150.00, 2, 4, '维修及时，价格合理'),
('RO20260601003', 1, 2, 6, 200.00, 1, NULL, NULL);

-- 示例跑腿需求（含紧急）
INSERT INTO errand_request (user_id, title, description, errand_type, pickup_address, delivery_address, urgency, reward, status) VALUES
(2, '代买退烧药', '急需布洛芬和体温计', '代买', '小区附近大参林药房', '3栋502室', 1, 20.00, 0),
(3, '代取大件快递', '菜鸟驿站有2个大件包裹', '代取', '菜鸟驿站', '5栋301室', 2, 15.00, 0),
(5, '代买水果', '买一箱苹果和香蕉', '代买', '小区门口水果店', '8栋301室', 2, 10.00, 0);

-- 示例跑腿订单
INSERT INTO errand_order (order_no, request_id, user_id, runner_id, amount, status, rating, comment) VALUES
('EO20260601001', 3, 4, 6, 12.00, 2, 5, '跑得很快，东西完好'),
('EO20260601002', 1, 2, 7, 15.00, 2, 4, '服务态度好'),
('EO20260601003', 2, 5, 8, 10.00, 1, NULL, NULL);

-- 示例论坛帖子
INSERT INTO forum_post (user_id, title, content, view_count, like_count, comment_count, status) VALUES
(2, '夏令营活动通知', '社区暑期夏令营开始报名啦！', 89, 12, 5, 1),
(3, '二手家电转让', '九成新冰箱洗衣机低价转让', 45, 6, 3, 1);

-- 示例评论
INSERT INTO forum_comment (post_id, user_id, content, like_count, status) VALUES
(4, 3, '夏令营多大孩子可以参加？', 2, 1),
(4, 5, '我家两个孩子都想报名！', 1, 1),
(5, 4, '冰箱还在吗？多少钱？', 0, 1);

-- 示例通知
INSERT INTO notification (user_id, title, content, type, is_read) VALUES
(1, '新跑腿需求', '3栋张女士发布紧急代买药品需求', 3, 0),
(1, '新商品待审核', '有3件新商品等待审核', 1, 0),
(1, '检修待分配', '有3个检修需求等待分配师傅', 3, 0);
