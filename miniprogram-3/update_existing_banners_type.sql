-- ========================================
-- 更新现有轮播图的类型
-- 将已存在的轮播图设置为首页轮播图
-- ========================================

-- 1. 查看当前所有轮播图的类型情况
SELECT id, title, type, image_url, status
FROM banners
ORDER BY id;

-- 2. 将所有 type 为 NULL 或 'home_banner' 的轮播图明确标记为首页轮播图
-- （已经是 company_gallery 的不会被改变）
UPDATE banners
SET type = 'home_banner'
WHERE type IS NULL
   OR type = 'home_banner';

-- 3. 查看更新后的结果
SELECT
    type,
    COUNT(*) as count,
    GROUP_CONCAT(title SEPARATOR ', ') as titles
FROM banners
GROUP BY type;

-- ========================================
-- 验证查询
-- ========================================

-- 查看首页轮播图
SELECT id, title, image_url, sort_order, status
FROM banners
WHERE type = 'home_banner' AND status = 1
ORDER BY sort_order ASC;

-- 查看公司风采图片
SELECT id, title, image_url, sort_order, status
FROM banners
WHERE type = 'company_gallery' AND status = 1
ORDER BY sort_order ASC;
