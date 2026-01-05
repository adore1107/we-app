-- =====================================================
-- 更新管理员密码为正确的BCrypt加密格式
-- 密码：admin123
-- =====================================================

USE `songjia_textile`;

-- 更新admin账号的密码为BCrypt加密后的 "admin123"
-- 使用在线BCrypt生成器生成的密码哈希
UPDATE `admins`
SET `password` = '$2a$10$X5wFuQBxDq4wZJ8rQk7B5.LJzV9FZ3qHzG3yKp6rJ7NxJB5pYqGHW'
WHERE `username` = 'admin';

-- 验证更新
SELECT id, username, real_name, role, status FROM admins WHERE username = 'admin';

-- =====================================================
-- 说明：
-- 原密码：admin123
-- BCrypt加密后：$2a$10$X5wFuQBxDq4wZJ8rQk7B5.LJzV9FZ3qHzG3yKp6rJ7NxJB5pYqGHW
-- =====================================================
