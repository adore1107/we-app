-- ========================================
-- 商品表改造：支持二级分类
-- 方案B：保存一级和二级分类ID
-- 时间：2025-12-29
-- ========================================

-- 1. 添加 main_category_id 字段（一级分类ID，先设为可选，迁移数据后再改为必填）
ALTER TABLE `products`
ADD COLUMN `main_category_id` INT NULL COMMENT '一级分类ID' AFTER `category_id`;

-- 2. 将 category_id 重命名为 sub_category_id（二级分类ID，可选）
ALTER TABLE `products`
CHANGE COLUMN `category_id` `sub_category_id` INT NULL COMMENT '二级分类ID（可选）';

-- 3. 添加索引优化查询
ALTER TABLE `products`
ADD INDEX `idx_main_category_id` (`main_category_id`);

ALTER TABLE `products`
ADD INDEX `idx_sub_category_id` (`sub_category_id`);

-- 添加组合索引，用于查询一级分类下的所有商品
ALTER TABLE `products`
ADD INDEX `idx_main_sub_category` (`main_category_id`, `sub_category_id`);

-- ========================================
-- 4. 迁移现有数据
-- ========================================

-- 先查看现有数据的分类情况
SELECT
    p.id AS product_id,
    p.sub_category_id AS current_category_id,
    c.id AS category_id,
    c.name AS category_name,
    c.level,
    c.parent_id
FROM products p
LEFT JOIN categories c ON p.sub_category_id = c.id
ORDER BY p.id
LIMIT 10;

-- 情况1：商品当前关联的是二级分类（level=2）
-- 设置 main_category_id 为其父分类ID，sub_category_id 保持不变
UPDATE products p
INNER JOIN categories c ON p.sub_category_id = c.id
SET p.main_category_id = c.parent_id
WHERE c.level = 2 AND c.parent_id IS NOT NULL;

-- 情况2：商品当前关联的是一级分类（level=1）
-- 设置 main_category_id 为该分类ID，sub_category_id 设为 NULL
UPDATE products p
INNER JOIN categories c ON p.sub_category_id = c.id
SET
    p.main_category_id = c.id,
    p.sub_category_id = NULL
WHERE c.level = 1;

-- 情况3：处理可能存在的孤儿数据（分类已被删除）
-- 将这些商品归到第一个一级分类下
UPDATE products p
SET
    p.main_category_id = (SELECT id FROM categories WHERE level = 1 ORDER BY id LIMIT 1),
    p.sub_category_id = NULL
WHERE p.main_category_id IS NULL OR p.main_category_id = 0;

-- 情况4：数据迁移完成后，将 main_category_id 改为必填字段
ALTER TABLE `products`
MODIFY COLUMN `main_category_id` INT NOT NULL COMMENT '一级分类ID';

-- ========================================
-- 5. 验证查询
-- ========================================

-- 查看迁移后的数据
SELECT
    p.id AS product_id,
    p.name AS product_name,
    mc.id AS main_cat_id,
    mc.name AS main_category,
    sc.id AS sub_cat_id,
    sc.name AS sub_category
FROM products p
LEFT JOIN categories mc ON p.main_category_id = mc.id
LEFT JOIN categories sc ON p.sub_category_id = sc.id
ORDER BY p.id
LIMIT 20;

-- 统计各分类下的商品数量
SELECT
    mc.name AS main_category,
    COUNT(DISTINCT p.id) AS total_products,
    COUNT(DISTINCT CASE WHEN p.sub_category_id IS NOT NULL THEN p.id END) AS with_sub_category,
    COUNT(DISTINCT CASE WHEN p.sub_category_id IS NULL THEN p.id END) AS without_sub_category
FROM products p
LEFT JOIN categories mc ON p.main_category_id = mc.id
GROUP BY mc.id, mc.name
ORDER BY mc.id;

-- 检查是否有商品没有正确设置一级分类
SELECT COUNT(*) AS orphan_products
FROM products
WHERE main_category_id IS NULL OR main_category_id = 0;

-- ========================================
-- 使用说明：
-- 1. 执行前请先备份数据库
-- 2. main_category_id 必填，指向一级分类
-- 3. sub_category_id 可选，指向二级分类
-- 4. 查询一级分类的所有商品：WHERE main_category_id = ?
-- 5. 查询二级分类的商品：WHERE sub_category_id = ?
-- 6. 查询只属于一级分类的商品：WHERE main_category_id = ? AND sub_category_id IS NULL
-- ========================================
