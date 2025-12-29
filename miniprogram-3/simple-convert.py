#!/usr/bin/env python3
import json
import pymysql

# 连接数据库
conn = pymysql.connect(
    host='localhost',
    user='root',
    password='123456',
    database='songjia_textile',
    charset='utf8mb4'
)

cursor = conn.cursor()

# 1. 先添加新的JSON字段
print("添加新字段...")
cursor.execute("ALTER TABLE products ADD COLUMN images1 JSON")
print("✅ 字段添加完成")

# 2. 读取所有数据并处理
print("开始处理数据...")
cursor.execute("SELECT id, images FROM products")
products = cursor.fetchall()

for product in products:
    product_id, images_str = product

    # 解析images为列表
    if images_str:
        try:
            # 尝试解析JSON
            images_list = json.loads(images_str)
        except:
            # 解析失败，按逗号分割
            images_list = [item.strip().strip('"\'') for item in images_str.split(',') if item.strip()]
    else:
        images_list = []

    # 转为JSON存入新字段
    json_str = json.dumps(images_list)
    cursor.execute("UPDATE products SET images1 = %s WHERE id = %s", (json_str, product_id))
    print(f"处理完成: ID {product_id}, 图片数 {len(images_list)}")

# 提交事务
conn.commit()
print("✅ 所有数据处理完成！")

# 关闭连接
cursor.close()
conn.close()