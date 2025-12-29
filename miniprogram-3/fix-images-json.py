#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
修复products表中images字段的JSON格式问题
"""

import json
import pymysql
import sys
from typing import List, Optional

def connect_mysql():
    """连接MySQL数据库"""
    try:
        connection = pymysql.connect(
            host='localhost',
            user='root',
            password='123456',
            database='songjia_textile',
            charset='utf8mb4'
        )
        print("✅ MySQL连接成功")
        return connection
    except Exception as e:
        print(f"❌ MySQL连接失败: {e}")
        sys.exit(1)

def check_data(connection):
    """检查现有数据状态"""
    cursor = connection.cursor()

    # 检查总数
    cursor.execute("SELECT COUNT(*) FROM products")
    total_count = cursor.fetchone()[0]
    print(f"📊 总商品数量: {total_count}")

    # 检查images字段状态
    cursor.execute("""
        SELECT
            COUNT(*) as total,
            SUM(CASE WHEN images IS NULL THEN 1 ELSE 0 END) as null_count,
            SUM(CASE WHEN images = '' THEN 1 ELSE 0 END) as empty_count,
            SUM(CASE WHEN JSON_VALID(images) = 1 THEN 1 ELSE 0 END) as valid_json_count,
            SUM(CASE WHEN JSON_VALID(images) = 0 AND images IS NOT NULL AND images != '' THEN 1 ELSE 0 END) as invalid_json_count
        FROM products
    """)

    result = cursor.fetchone()
    print(f"📈 数据状态分析:")
    print(f"  - NULL值: {result[1]}")
    print(f"  - 空字符串: {result[2]}")
    print(f"  - 有效JSON: {result[3]}")
    print(f"  - 无效JSON: {result[4]}")

    # 查看几个无效JSON的例子
    cursor.execute("""
        SELECT id, name, images
        FROM products
        WHERE JSON_VALID(images) = 0 AND images IS NOT NULL AND images != ''
        LIMIT 3
    """)

    invalid_examples = cursor.fetchall()
    if invalid_examples:
        print(f"❌ 无效JSON示例:")
        for example in invalid_examples:
            print(f"  ID: {example[0]}, 名称: {example[1]}")
            print(f"  Images: {example[2]}")

    cursor.close()
    return result

def fix_invalid_json(connection):
    """修复无效的JSON数据"""
    cursor = connection.cursor()

    print("🔧 开始修复无效JSON数据...")

    try:
        # 1. 将NULL和空值设置为空数组
        cursor.execute("""
            UPDATE products
            SET images = '[]'
            WHERE images IS NULL OR images = ''
        """)
        null_empty_fixed = cursor.rowcount
        print(f"  ✅ 修复NULL/空值: {null_empty_fixed} 条")

        # 2. 修复无效的JSON数据
        cursor.execute("""
            SELECT id, images
            FROM products
            WHERE JSON_VALID(images) = 0 AND images IS NOT NULL
        """)
        invalid_records = cursor.fetchall()

        fixed_count = 0
        for record in invalid_records:
            try:
                # 尝试修复JSON
                images_str = record[1]
                print(f"  🔍 修复记录ID: {record[0]}, 原数据: {images_str[:50]}...")

                # 如果看起来像JSON但格式错误，尝试修复
                if images_str.startswith('[') or images_str.startswith('{'):
                    # 移除可能的额外引号
                    cleaned = images_str.strip()
                    if cleaned.startswith('"') and cleaned.endswith('"'):
                        cleaned = cleaned[1:-1]

                    # 验证修复后的JSON
                    try:
                        json.loads(cleaned)
                        cursor.execute("UPDATE products SET images = %s WHERE id = %s", (cleaned, record[0]))
                        print(f"    ✅ 修复成功: {cleaned[:30]}...")
                        fixed_count += 1
                    except json.JSONDecodeError:
                        # 修复失败，设为空数组
                        cursor.execute("UPDATE products SET images = %s WHERE id = %s", ('[]', record[0]))
                        print(f"    ⚠️  无法修复，设为空数组")
                        fixed_count += 1
                else:
                    # 不像JSON，直接设为空数组
                    cursor.execute("UPDATE products SET images = %s WHERE id = %s", ('[]', record[0]))
                    print(f"    ⚠️  不是JSON格式，设为空数组")
                    fixed_count += 1

            except Exception as e:
                print(f"    ❌ 修复失败: {e}")

        connection.commit()
        print(f"  ✅ 修复完成，共处理 {fixed_count + len(invalid_records)} 条记录")

    except Exception as e:
        print(f"❌ 修复过程中出错: {e}")
        connection.rollback()
    finally:
        cursor.close()

def alter_table_to_json(connection):
    """将字段类型改为JSON"""
    cursor = connection.cursor()

    try:
        print("🔧 将images字段改为JSON类型...")
        cursor.execute("ALTER TABLE products MODIFY COLUMN images JSON")
        connection.commit()
        print("  ✅ 字段类型修改成功")

        # 验证修改结果
        cursor.execute("DESCRIBE products")
        columns = cursor.fetchall()
        for column in columns:
            if column[0] == 'images':
                print(f"  📋 字段信息: {column}")
                break

    except Exception as e:
        print(f"❌ 修改字段类型失败: {e}")
        connection.rollback()
    finally:
        cursor.close()

def verify_fix(connection):
    """验证修复结果"""
    cursor = connection.cursor()

    cursor.execute("""
        SELECT
            COUNT(*) as total,
            SUM(CASE WHEN JSON_VALID(images) = 1 THEN 1 ELSE 0 END) as valid_json_count
        FROM products
    """)

    result = cursor.fetchone()
    print(f"🎉 修复结果验证:")
    print(f"  - 总记录数: {result[0]}")
    print(f"  - 有效JSON: {result[1]}")
    print(f"  - 修复率: {(result[1]/result[0]*100):.1f}%")

    # 显示几个修复后的例子
    cursor.execute("""
        SELECT id, name, images
        FROM products
        WHERE JSON_VALID(images) = 1
        LIMIT 3
    """)

    examples = cursor.fetchall()
    if examples:
        print(f"✅ 修复后示例:")
        for example in examples:
            print(f"  ID: {example[0]}, Images: {example[2]}")

    cursor.close()

def main():
    """主函数"""
    print("🚀 开始修复products表的images字段JSON格式问题")
    print("=" * 60)

    # 连接数据库
    connection = connect_mysql()

    try:
        # 1. 检查当前数据状态
        print("\n📊 第1步: 检查数据状态")
        check_result = check_data(connection)

        # 2. 修复无效JSON数据
        print("\n🔧 第2步: 修复无效JSON数据")
        fix_invalid_json(connection)

        # 3. 改为JSON类型
        print("\n🔧 第3步: 修改字段类型")
        alter_table_to_json(connection)

        # 4. 验证修复结果
        print("\n✅ 第4步: 验证修复结果")
        verify_fix(connection)

        print("\n🎉 所有步骤完成！images字段现在是JSON类型了")

    except Exception as e:
        print(f"❌ 执行过程中出错: {e}")
    finally:
        connection.close()
        print("🔌 数据库连接已关闭")

if __name__ == "__main__":
    main()