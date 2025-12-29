-- ========================================
-- 商品分类表改造：支持二级分类
-- 时间：2025-12-29
-- ========================================

-- 1. 添加 parent_id 字段（父分类ID）
ALTER TABLE `categories`
ADD COLUMN `parent_id` INT NULL DEFAULT NULL COMMENT '父分类ID，NULL表示一级分类' AFTER `id`;

-- 2. 添加 level 字段（分类层级）
ALTER TABLE `categories`
ADD COLUMN `level` TINYINT NOT NULL DEFAULT 1 COMMENT '分类层级：1=一级分类，2=二级分类' AFTER `parent_id`;

-- 3. 添加索引优化查询
ALTER TABLE `categories`
ADD INDEX `idx_parent_id` (`parent_id`);

ALTER TABLE `categories`
ADD INDEX `idx_level` (`level`);

-- 4. 更新现有数据（将所有现有分类标记为一级分类）
UPDATE `categories` SET `level` = 1, `parent_id` = NULL WHERE `parent_id` IS NULL;

-- ========================================
-- 插入二级分类数据（根据实际业务分类）
-- ========================================

-- 实际一级分类ID：
-- ID=1: 凝胶床垫系列
-- ID=2: 天丝床品系列
-- ID=3: 科技布沙发系列
-- ID=4: 冰丝凉席系列
-- ID=5: 功能性面料

-- 为 "凝胶床垫系列"(ID=1) 添加二级分类
INSERT INTO `categories` (`parent_id`, `level`, `name`, `icon`, `sort_order`, `status`, `created_at`) VALUES
(1, 2, '记忆凝胶床垫', NULL, 1, 1, NOW()),
(1, 2, '凉感凝胶床垫', NULL, 2, 1, NOW()),
(1, 2, '乳胶凝胶床垫', NULL, 3, 1, NOW()),
(1, 2, '护脊凝胶床垫', NULL, 4, 1, NOW());

-- 为 "天丝床品系列"(ID=2) 添加二级分类
INSERT INTO `categories` (`parent_id`, `level`, `name`, `icon`, `sort_order`, `status`, `created_at`) VALUES
(2, 2, '天丝四件套', NULL, 1, 1, NOW()),
(2, 2, '天丝被套', NULL, 2, 1, NOW()),
(2, 2, '天丝床单', NULL, 3, 1, NOW()),
(2, 2, '天丝枕套', NULL, 4, 1, NOW());

-- 为 "科技布沙发系列"(ID=3) 添加二级分类
INSERT INTO `categories` (`parent_id`, `level`, `name`, `icon`, `sort_order`, `status`, `created_at`) VALUES
(3, 2, '科技布三人沙发', NULL, 1, 1, NOW()),
(3, 2, '科技布转角沙发', NULL, 2, 1, NOW()),
(3, 2, '科技布单人沙发', NULL, 3, 1, NOW()),
(3, 2, '科技布沙发套', NULL, 4, 1, NOW());

-- 为 "冰丝凉席系列"(ID=4) 添加二级分类
INSERT INTO `categories` (`parent_id`, `level`, `name`, `icon`, `sort_order`, `status`, `created_at`) VALUES
(4, 2, '冰丝三件套凉席', NULL, 1, 1, NOW()),
(4, 2, '冰丝单件凉席', NULL, 2, 1, NOW()),
(4, 2, '冰丝枕席', NULL, 3, 1, NOW()),
(4, 2, '冰丝空调席', NULL, 4, 1, NOW());

-- 为 "功能性面料"(ID=5) 添加二级分类
INSERT INTO `categories` (`parent_id`, `level`, `name`, `icon`, `sort_order`, `status`, `created_at`) VALUES
(5, 2, '抗菌面料', NULL, 1, 1, NOW()),
(5, 2, '防水面料', NULL, 2, 1, NOW()),
(5, 2, '防静电面料', NULL, 3, 1, NOW()),
(5, 2, '阻燃面料', NULL, 4, 1, NOW());

-- ========================================
-- 查询验证
-- ========================================

-- 查看所有一级分类
SELECT id, name, level, parent_id, sort_order
FROM categories
WHERE level = 1
ORDER BY sort_order;

-- 查看所有二级分类及其父分类
SELECT
    c2.id AS sub_id,
    c2.name AS sub_name,
    c1.id AS parent_id,
    c1.name AS parent_name,
    c2.sort_order
FROM categories c2
LEFT JOIN categories c1 ON c2.parent_id = c1.id
WHERE c2.level = 2
ORDER BY c1.sort_order, c2.sort_order;

-- 查看树形分类结构
SELECT
    CASE
        WHEN c.level = 1 THEN c.name
        WHEN c.level = 2 THEN CONCAT('  └─ ', c.name)
    END AS category_tree,
    c.id,
    c.level,
    c.parent_id,
    c.sort_order
FROM categories c
ORDER BY
    COALESCE(c.parent_id, c.id),
    c.level,
    c.sort_order;

-- ========================================
-- 使用说明：
-- 1. 执行前请先备份数据库
-- 2. 示例数据中的一级分类ID需要根据实际情况调整
-- 3. 可以根据实际业务需求调整二级分类内容
-- 4. 外键约束可选，如果不需要可以注释掉
-- ========================================
