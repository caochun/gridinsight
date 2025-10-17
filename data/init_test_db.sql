-- SQLite测试数据库初始化脚本
-- 创建配变表
CREATE TABLE IF NOT EXISTS transformers (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'active',
    capacity REAL,
    location TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 创建用户表
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    active INTEGER NOT NULL DEFAULT 1,
    transformer_id INTEGER,
    address TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (transformer_id) REFERENCES transformers(id)
);

-- 创建历史负荷表
CREATE TABLE IF NOT EXISTS historical_load (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    load_value REAL NOT NULL,
    date DATETIME NOT NULL,
    location TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 插入测试数据 - 配变数据
INSERT INTO transformers (name, status, capacity, location) VALUES 
('配变001', 'active', 315.0, '北京市朝阳区'),
('配变002', 'active', 400.0, '北京市海淀区'),
('配变003', 'active', 500.0, '北京市西城区'),
('配变004', 'inactive', 315.0, '北京市东城区'),
('配变005', 'active', 630.0, '北京市丰台区'),
('配变006', 'active', 400.0, '北京市石景山区'),
('配变007', 'active', 500.0, '北京市通州区'),
('配变008', 'maintenance', 315.0, '北京市大兴区'),
('配变009', 'active', 630.0, '北京市昌平区'),
('配变010', 'active', 400.0, '北京市房山区');

-- 插入测试数据 - 用户数据
INSERT INTO users (name, active, transformer_id, address) VALUES 
('张三', 1, 1, '朝阳区某小区1号楼'),
('李四', 1, 1, '朝阳区某小区2号楼'),
('王五', 1, 2, '海淀区某小区3号楼'),
('赵六', 1, 2, '海淀区某小区4号楼'),
('钱七', 1, 3, '西城区某小区5号楼'),
('孙八', 1, 3, '西城区某小区6号楼'),
('周九', 1, 5, '丰台区某小区7号楼'),
('吴十', 1, 5, '丰台区某小区8号楼'),
('郑十一', 1, 6, '石景山区某小区9号楼'),
('王十二', 1, 6, '石景山区某小区10号楼'),
('李十三', 1, 7, '通州区某小区11号楼'),
('张十四', 1, 7, '通州区某小区12号楼'),
('刘十五', 1, 9, '昌平区某小区13号楼'),
('陈十六', 1, 9, '昌平区某小区14号楼'),
('杨十七', 1, 10, '房山区某小区15号楼'),
('黄十八', 1, 10, '房山区某小区16号楼'),
('吴十九', 0, 4, '东城区某小区17号楼'), -- 非活跃用户
('林二十', 0, 8, '大兴区某小区18号楼'); -- 非活跃用户

-- 插入测试数据 - 历史负荷数据（最近7天）
INSERT INTO historical_load (load_value, date, location) VALUES 
-- 7天前
(1250.5, datetime('now', '-7 days'), '北京电网'),
(1180.3, datetime('now', '-7 days', '+1 hour'), '北京电网'),
(1320.8, datetime('now', '-7 days', '+2 hours'), '北京电网'),
(1450.2, datetime('now', '-7 days', '+3 hours'), '北京电网'),
-- 6天前
(1280.7, datetime('now', '-6 days'), '北京电网'),
(1200.1, datetime('now', '-6 days', '+1 hour'), '北京电网'),
(1350.9, datetime('now', '-6 days', '+2 hours'), '北京电网'),
(1420.6, datetime('now', '-6 days', '+3 hours'), '北京电网'),
-- 5天前
(1300.4, datetime('now', '-5 days'), '北京电网'),
(1220.8, datetime('now', '-5 days', '+1 hour'), '北京电网'),
(1380.5, datetime('now', '-5 days', '+2 hours'), '北京电网'),
(1480.3, datetime('now', '-5 days', '+3 hours'), '北京电网'),
-- 4天前
(1320.6, datetime('now', '-4 days'), '北京电网'),
(1250.2, datetime('now', '-4 days', '+1 hour'), '北京电网'),
(1400.7, datetime('now', '-4 days', '+2 hours'), '北京电网'),
(1500.1, datetime('now', '-4 days', '+3 hours'), '北京电网'),
-- 3天前
(1350.8, datetime('now', '-3 days'), '北京电网'),
(1280.4, datetime('now', '-3 days', '+1 hour'), '北京电网'),
(1420.9, datetime('now', '-3 days', '+2 hours'), '北京电网'),
(1520.5, datetime('now', '-3 days', '+3 hours'), '北京电网'),
-- 2天前
(1380.2, datetime('now', '-2 days'), '北京电网'),
(1310.6, datetime('now', '-2 days', '+1 hour'), '北京电网'),
(1450.3, datetime('now', '-2 days', '+2 hours'), '北京电网'),
(1550.8, datetime('now', '-2 days', '+3 hours'), '北京电网'),
-- 1天前
(1400.5, datetime('now', '-1 day'), '北京电网'),
(1330.9, datetime('now', '-1 day', '+1 hour'), '北京电网'),
(1480.6, datetime('now', '-1 day', '+2 hours'), '北京电网'),
(1580.2, datetime('now', '-1 day', '+3 hours'), '北京电网'),
-- 今天
(1420.8, datetime('now'), '北京电网'),
(1350.3, datetime('now', '+1 hour'), '北京电网'),
(1500.1, datetime('now', '+2 hours'), '北京电网'),
(1600.7, datetime('now', '+3 hours'), '北京电网');

-- 创建索引以提高查询性能
CREATE INDEX IF NOT EXISTS idx_transformers_status ON transformers(status);
CREATE INDEX IF NOT EXISTS idx_users_active ON users(active);
CREATE INDEX IF NOT EXISTS idx_historical_load_date ON historical_load(date);

-- 显示数据统计
SELECT '配变总数' as metric, COUNT(*) as value FROM transformers WHERE status = 'active'
UNION ALL
SELECT '用户总数' as metric, COUNT(*) as value FROM users WHERE active = 1
UNION ALL
SELECT '历史负荷平均值' as metric, ROUND(AVG(load_value), 2) as value FROM historical_load WHERE date >= datetime('now', '-1 day');
