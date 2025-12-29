-- 修复商品分类数据：将 category_id = 0 的记录更新为 NULL
-- 这样可以解决 Category 实体找不到导致的 StackOverflowError

-- 查看当前有多少商品的 category_id = 0
SELECT
    'Products with category_id = 0:' as info,
    COUNT(*) as count
FROM products
WHERE category_id = 0;

-- 查看这些商品的详细信息
SELECT
    id,
    name,
    category_id,
    status,
    created_at
FROM products
WHERE category_id = 0;

-- 将 category_id = 0 的商品更新为 NULL
-- NULL 表示未分类商品，符合我们的新设计
UPDATE products
SET category_id = NULL
WHERE category_id = 0;

-- 验证更新结果
SELECT
    'Products updated to NULL category:' as info,
    COUNT(*) as count
FROM products
WHERE category_id IS NULL;

-- 确认没有 category_id = 0 的记录了
SELECT
    'Remaining products with category_id = 0:' as info,
    COUNT(*) as count
FROM products
WHERE category_id = 0;

-- 显示最终的分类分布
SELECT
    CASE
        WHEN category_id IS NULL THEN '未分类'
        WHEN category_id = 0 THEN '错误数据(应该是0)'
        ELSE CAST(category_id AS CHAR)
    END as category_group,
    COUNT(*) as product_count
FROM products
GROUP BY
    CASE
        WHEN category_id IS NULL THEN '未分类'
        WHEN category_id = 0 THEN '错误数据(应该是0)'
        ELSE CAST(category_id AS CHAR)
    END
ORDER BY product_count DESC;