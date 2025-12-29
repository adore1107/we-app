-- ========================================
-- 为 banners 表添加 type 字段
-- 用途：区分首页轮播图和公司风采图片
-- ========================================

-- 1. 添加 type 字段
ALTER TABLE `banners`
ADD COLUMN `type` enum('home_banner','company_gallery','product_banner')
DEFAULT 'home_banner'
COMMENT '图片类型：home_banner-首页轮播，company_gallery-公司风采，product_banner-商品轮播'
AFTER `id`;

-- 2. 添加索引优化查询
ALTER TABLE `banners`
ADD INDEX `idx_type` (`type`);

-- 3. 更新已有数据（将现有数据标记为首页轮播图）
UPDATE `banners` SET `type` = 'home_banner' WHERE `type` IS NULL;

-- 4. 插入公司风采示例数据
INSERT INTO `banners` (`type`, `title`, `image_url`, `link_url`, `link_type`, `sort_order`, `status`, `created_at`) VALUES
('company_gallery', '现代化生产车间 - 自动化织造设备', 'https://your-cdn.com/company/workshop.jpg', NULL, 'none', 1, 1, NOW()),
('company_gallery', '质量检测中心 - 严格品控流程', 'https://your-cdn.com/company/quality-control.jpg', NULL, 'none', 2, 1, NOW()),
('company_gallery', '产品展示厅 - 千余种面料样品', 'https://your-cdn.com/company/showroom.jpg', NULL, 'none', 3, 1, NOW()),
('company_gallery', '企业办公大楼 - 现代化办公环境', 'https://your-cdn.com/company/office-building.jpg', NULL, 'none', 4, 1, NOW()),
('company_gallery', '仓储物流中心 - 高效配送体系', 'https://your-cdn.com/company/warehouse.jpg', NULL, 'none', 5, 1, NOW());

-- ========================================
-- 使用说明：
-- 1. 首页轮播图: type = 'home_banner'
-- 2. 公司风采图: type = 'company_gallery'
-- 3. 商品轮播图: type = 'product_banner' (预留)
-- ========================================
