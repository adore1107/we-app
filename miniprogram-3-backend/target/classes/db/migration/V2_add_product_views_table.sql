-- =====================================================
-- 商品浏览统计优化：新增 product_views 表
-- 功能：记录用户浏览商品的行为，支持UV/PV分析
-- 创建时间：2025-12-30
-- =====================================================

USE `songjia_textile`;

-- 创建商品浏览记录表（简化版：不做去重，记录每次点击）
CREATE TABLE IF NOT EXISTS `product_views` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '浏览记录ID',
  `user_id` int NOT NULL COMMENT '用户ID',
  `product_id` int NOT NULL COMMENT '商品ID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '浏览时间',
  PRIMARY KEY (`id`),
  -- 普通索引：优化常见查询
  KEY `idx_product_id` (`product_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_created_at` (`created_at` DESC),
  KEY `idx_user_product` (`user_id`, `product_id`),
  -- 外键约束
  CONSTRAINT `fk_view_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_view_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品浏览记录表（记录每次用户点击）';

-- =====================================================
-- 可选优化：为 products 表添加排序功能所需的索引
-- （提升"综合/最新/热门"排序性能）
-- =====================================================

-- 优化"最新"排序（ORDER BY created_at DESC）
ALTER TABLE `products` ADD INDEX `idx_created_at` (`created_at` DESC);

-- 优化"热门"排序（ORDER BY view_count DESC, favorite_count DESC）
ALTER TABLE `products` ADD INDEX `idx_view_favorite` (`view_count` DESC, `favorite_count` DESC);

-- =====================================================
-- 数据说明
-- =====================================================
-- 1. products.view_count: 商品总浏览量（每次访问+1）
-- 2. product_views: 详细浏览记录（每次访问新增一条记录）
-- 3. 可通过 COUNT(DISTINCT user_id) 统计独立访客数（UV）
-- 4. 可通过 COUNT(*) 统计总浏览次数（PV）
-- 5. 一个用户可以多次访问同一商品，每次都会被记录
-- =====================================================
