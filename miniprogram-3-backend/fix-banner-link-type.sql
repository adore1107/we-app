-- 修复Banner表link_type字段的大小写问题
-- 将数据库中的小写 'none' 改为大写 'NONE' 以匹配Java枚举

UPDATE banners
SET link_type = 'NONE'
WHERE link_type = 'none';

-- 检查是否还有其他需要修复的情况
SELECT * FROM banners WHERE link_type NOT IN ('NONE', 'PRODUCT', 'CATEGORY', 'URL');