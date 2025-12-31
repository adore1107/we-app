-- =====================================================
-- 商品浏览统计升级：添加浏览时长追踪
-- 功能：记录用户在商品详情页停留的时间
-- 创建时间：2025-12-30
-- =====================================================

USE `songjia_textile`;

-- 修改 product_views 表，添加浏览时长相关字段
ALTER TABLE `product_views`
  ADD COLUMN `view_start_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '进入页面时间',
  ADD COLUMN `view_end_time` timestamp NULL DEFAULT NULL COMMENT '离开页面时间',
  ADD COLUMN `duration_seconds` int DEFAULT 0 COMMENT '浏览时长（秒）',
  MODIFY COLUMN `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间';

-- 添加索引优化时长统计查询
ALTER TABLE `product_views`
  ADD INDEX `idx_duration` (`duration_seconds` DESC);

-- =====================================================
-- 使用说明
-- =====================================================
-- 1. view_start_time: 用户进入商品详情页的时间（插入记录时自动设置）
-- 2. view_end_time: 用户离开页面的时间（离开时更新）
-- 3. duration_seconds: 浏览时长 = view_end_time - view_start_time（秒）
-- 4. 如果用户没有正常离开（如关闭小程序），view_end_time 为 NULL
-- =====================================================
