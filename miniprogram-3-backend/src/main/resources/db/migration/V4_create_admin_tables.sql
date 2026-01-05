-- =====================================================
-- 后台管理系统：管理员和日志表
-- 创建时间：2025-12-31
-- =====================================================

USE `songjia_textile`;

-- 创建管理员表
CREATE TABLE IF NOT EXISTS `admins` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
  `username` varchar(50) NOT NULL UNIQUE COMMENT '用户名（登录账号）',
  `password` varchar(255) NOT NULL COMMENT '密码（BCrypt加密）',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `role` enum('super_admin','product_admin','customer_service','analyst') DEFAULT 'product_admin' COMMENT '角色：super_admin-超级管理员，product_admin-商品管理员，customer_service-客服，analyst-数据分析员',
  `status` bit(1) DEFAULT b'1' COMMENT '状态：0-禁用，1-启用',
  `last_login_at` timestamp NULL DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(50) DEFAULT NULL COMMENT '最后登录IP',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_status` (`status`),
  KEY `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员表';

-- 创建管理员操作日志表
CREATE TABLE IF NOT EXISTS `admin_logs` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `admin_id` int NOT NULL COMMENT '管理员ID',
  `admin_name` varchar(50) DEFAULT NULL COMMENT '管理员用户名（冗余字段）',
  `module` varchar(50) DEFAULT NULL COMMENT '模块：product-商品，category-分类，banner-轮播图等',
  `action` varchar(100) DEFAULT NULL COMMENT '操作类型：create-新增，update-修改，delete-删除，login-登录等',
  `content` text COMMENT '操作内容（JSON格式）',
  `ip` varchar(50) DEFAULT NULL COMMENT 'IP地址',
  `user_agent` varchar(255) DEFAULT NULL COMMENT '浏览器UA',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_admin_id` (`admin_id`),
  KEY `idx_module` (`module`),
  KEY `idx_action` (`action`),
  KEY `idx_created_at` (`created_at` DESC),
  CONSTRAINT `fk_log_admin` FOREIGN KEY (`admin_id`) REFERENCES `admins` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员操作日志表';

-- 插入默认超级管理员（用户名：admin，密码：admin123）
-- 密码使用BCrypt加密，需要在Java代码中生成
-- 这里先插入明文，后续通过代码更新为加密密码
INSERT INTO `admins` (`username`, `password`, `real_name`, `role`, `status`)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7q5YbqQyC', '系统管理员', 'super_admin', b'1');
-- 密码：admin123 的BCrypt加密结果

-- =====================================================
-- 说明
-- =====================================================
-- 1. admins表存储后台管理员账号
-- 2. 密码使用BCrypt加密（Spring Security标准）
-- 3. role字段定义4种角色，可扩展权限系统
-- 4. admin_logs表记录所有管理员操作，用于审计
-- 5. 默认管理员账号 admin/admin123，首次登录后请修改密码
-- =====================================================
