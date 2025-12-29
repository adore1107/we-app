-- ========================================
-- 将商品随机分配到二级分类
-- 每个一级分类下的商品将被均匀分配到其4个二级分类中
-- ========================================

-- 查看当前商品分布情况
SELECT
    main_category_id,
    COUNT(*) as product_count
FROM products
GROUP BY main_category_id
ORDER BY main_category_id;

-- ========================================
-- 开始分配
-- ========================================

-- 1. 凝胶床垫系列(main_category_id=1) -> 二级分类: 10,11,12,13
-- 使用 MOD(id, 4) 来均匀分配到4个子分类
UPDATE products
SET sub_category_id = CASE
    WHEN MOD(id, 4) = 0 THEN 10  -- 记忆凝胶床垫
    WHEN MOD(id, 4) = 1 THEN 11  -- 凉感凝胶床垫
    WHEN MOD(id, 4) = 2 THEN 12  -- 乳胶凝胶床垫
    WHEN MOD(id, 4) = 3 THEN 13  -- 护脊凝胶床垫
END
WHERE main_category_id = 1;

-- 2. 天丝床品系列(main_category_id=2) -> 二级分类: 14,15,16,17
UPDATE products
SET sub_category_id = CASE
    WHEN MOD(id, 4) = 0 THEN 14  -- 天丝四件套
    WHEN MOD(id, 4) = 1 THEN 15  -- 天丝被套
    WHEN MOD(id, 4) = 2 THEN 16  -- 天丝床单
    WHEN MOD(id, 4) = 3 THEN 17  -- 天丝枕套
END
WHERE main_category_id = 2;

-- 3. 科技布沙发系列(main_category_id=3) -> 二级分类: 18,19,20,21
UPDATE products
SET sub_category_id = CASE
    WHEN MOD(id, 4) = 0 THEN 18  -- 科技布三人沙发
    WHEN MOD(id, 4) = 1 THEN 19  -- 科技布转角沙发
    WHEN MOD(id, 4) = 2 THEN 20  -- 科技布单人沙发
    WHEN MOD(id, 4) = 3 THEN 21  -- 科技布沙发套
END
WHERE main_category_id = 3;

-- 4. 冰丝凉席系列(main_category_id=4) -> 二级分类: 22,23,24,25
UPDATE products
SET sub_category_id = CASE
    WHEN MOD(id, 4) = 0 THEN 22  -- 冰丝三件套凉席
    WHEN MOD(id, 4) = 1 THEN 23  -- 冰丝单件凉席
    WHEN MOD(id, 4) = 2 THEN 24  -- 冰丝枕席
    WHEN MOD(id, 4) = 3 THEN 25  -- 冰丝空调席
END
WHERE main_category_id = 4;

-- 5. 功能性面料(main_category_id=5) -> 二级分类: 26,27,28,29
UPDATE products
SET sub_category_id = CASE
    WHEN MOD(id, 4) = 0 THEN 26  -- 抗菌面料
    WHEN MOD(id, 4) = 1 THEN 27  -- 防水面料
    WHEN MOD(id, 4) = 2 THEN 28  -- 防静电面料
    WHEN MOD(id, 4) = 3 THEN 29  -- 阻燃面料
END
WHERE main_category_id = 5;

-- ========================================
-- 验证分配结果
-- ========================================

-- 查看每个一级分类下各个二级分类的商品数量
SELECT
    mc.name AS main_category,
    sc.name AS sub_category,
    COUNT(p.id) AS product_count
FROM products p
LEFT JOIN categories mc ON p.main_category_id = mc.id
LEFT JOIN categories sc ON p.sub_category_id = sc.id
GROUP BY mc.id, mc.name, sc.id, sc.name
ORDER BY mc.id, sc.id;

-- 查看前20个商品的分配情况
SELECT
    p.id,
    p.name AS product_name,
    mc.name AS main_category,
    sc.name AS sub_category
FROM products p
LEFT JOIN categories mc ON p.main_category_id = mc.id
LEFT JOIN categories sc ON p.sub_category_id = sc.id
ORDER BY p.id
LIMIT 20;

-- 检查是否有商品没有分配到二级分类
SELECT COUNT(*) AS products_without_subcategory
FROM products
WHERE sub_category_id IS NULL;
