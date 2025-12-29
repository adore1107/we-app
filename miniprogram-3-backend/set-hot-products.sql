-- 设置一些商品为热门商品
-- 确保这些商品是上架状态 (status = 1)

-- 查看当前商品状态
SELECT
    id,
    name,
    is_hot,
    status,
    favorite_count,
    view_count,
    created_at
FROM products
WHERE status = 1
ORDER BY favorite_count DESC, view_count DESC
LIMIT 10;

-- 将收藏量和浏览量最高的前5个商品设置为热门商品
UPDATE products
SET is_hot = 1
WHERE id IN (
    SELECT id FROM (
        SELECT id
        FROM products
        WHERE status = 1
        ORDER BY favorite_count DESC, view_count DESC
        LIMIT 5
    ) AS top_products
);

-- 验证设置结果
SELECT
    id,
    name,
    is_hot,
    status,
    favorite_count,
    view_count
FROM products
WHERE status = 1 AND is_hot = 1
ORDER BY favorite_count DESC, view_count DESC;

-- 确认热门商品数量
SELECT
    COUNT(*) as hot_products_count
FROM products
WHERE status = 1 AND is_hot = 1;