# 移除外键约束说明

## 🎯 目标
移除 products 和 categories 表之间的外键约束，改为非关联关系。

## ✅ 已完成的代码修改

### 1. 数据库设计文档
- ✅ 更新了 `simple-database-design.md`
- ✅ 移除了 `FOREIGN KEY (category_id) REFERENCES categories(id)`

### 2. JPA实体类
- ✅ Product.java: 移除了 `@ManyToOne` 和 `@JoinColumn` 注解
- ✅ Product.java: 将 `Category category` 改为 `Integer categoryId`
- ✅ Category.java: 移除了 `@OneToMany` 关联关系
- ✅ 删除了不必要的 imports

### 3. 服务层
- ✅ ProductService.java: 更新了分类查询逻辑，支持 `categoryId = null`

### 4. Repository层
- ✅ 添加了 `findByStatusOrderBySortOrderDesc(Boolean status)` 方法

## 🚀 需要执行的操作

### 第一步：执行SQL脚本
```sql
-- 方式1：使用提供的脚本
mysql -u root -p123456 songjia_textile < remove-foreign-keys.sql

-- 方式2：手动执行（如果知道外键约束名称）
mysql -u root -p123456 songjia_textile
-- 先查看外键约束：
SELECT CONSTRAINT_NAME FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
WHERE CONSTRAINT_SCHEMA = 'songjia_textile' AND CONSTRAINT_TYPE = 'FOREIGN KEY' AND TABLE_NAME = 'products';

-- 然后删除约束（替换 YOUR_CONSTRAINT_NAME）：
ALTER TABLE products DROP FOREIGN KEY YOUR_CONSTRAINT_NAME;
```

### 第二步：重启后端应用
```bash
# 停止当前应用
# 然后重启
cd C:\Users\Mayn\WeChatProjects\miniprogram-3-backend
start.bat
```

### 第三步：验证修改
重启后，检查数据库：
```sql
-- 确认products表结构
DESCRIBE products;

-- 确认没有外键约束
SELECT
    CONSTRAINT_NAME,
    TABLE_NAME,
    REFERENCED_TABLE_NAME
FROM
    INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE
    REFERENCED_TABLE_SCHEMA = 'songjia_textile'
    AND TABLE_NAME = 'products';
```

## 📋 修改后的效果

### 数据库层面：
- ✅ `category_id` 字段可以为 NULL
- ✅ 没有 `FOREIGN KEY` 约束
- ✅ 数据完整性由应用层控制

### Java代码层面：
- ✅ Product.categoryId（Integer类型）存储分类ID
- ✅ 不再有 JPA实体关联关系
- ✅ 查询更加灵活

### 业务逻辑：
- ✅ `categoryId = null` 表示未分类商品
- ✅ 分类查询仍然可以正常工作
- ✅ 数据关系更加灵活

## 🛠️ 备份方案

在执行SQL之前，建议备份数据：
```sql
-- 备份products表
CREATE TABLE products_backup AS SELECT * FROM products;

-- 如果需要恢复
-- INSERT INTO products SELECT * FROM products_backup;
-- DROP TABLE products;
-- RENAME TABLE products_backup TO products;
```

## 📱 优势

1. **灵活性提高** - 可以删除分类而不影响商品
2. **性能提升** - 没有外键检查的开销
3. **维护简化** - 数据迁移和重构更容易
4. **业务控制** - 应用层控制数据完整性

## ⚠️ 注意事项

1. **数据一致性** - 应用层需要确保categoryId指向有效的分类ID
2. **清理工作** - 删除分类时需要手动处理相关商品的categoryId
3. **测试验证** - 确保所有相关功能正常工作