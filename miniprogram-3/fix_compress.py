#!/usr/bin/env python3
"""
修复图片压缩问题
先恢复备份的图片，然后正确压缩
"""

import os
import sys
from pathlib import Path

def install_pil_if_needed():
    """检查并安装PIL库"""
    try:
        from PIL import Image
        return True
    except ImportError:
        print("❌ 需要安装PIL库")
        print("请运行以下命令安装：")
        print("pip install Pillow")
        return False

def restore_backup_images():
    """恢复备份的图片文件"""
    images_dir = Path("images")
    if not images_dir.exists():
        print(f"❌ 找不到images目录")
        return False

    print("🔄 恢复备份图片...")
    for backup_file in images_dir.glob("*.backup"):
        # 移除.backup后缀，恢复原名
        original_name = backup_file.stem  # 获取不带.backup的名字
        original_path = images_dir / original_name

        print(f"📦 恢复: {backup_file.name} → {original_name}")
        backup_file.rename(original_path)

    return True

def compress_image_file(image_path, target_width=750, target_height=300, quality=90):
    """压缩单个图片文件"""
    try:
        from PIL import Image

        with Image.open(image_path) as img:
            print(f"📷 处理: {image_path.name}")
            original_width, original_height = img.size
            original_size = os.path.getsize(image_path) / 1024

            print(f"   原始: {original_width}×{original_height}px, {original_size:.1f}KB")

            # 转换为RGB模式
            if img.mode != 'RGB':
                img = img.convert('RGB')

            # 计算缩放比例
            width_ratio = target_width / original_width
            height_ratio = target_height / original_height
            scale_ratio = max(width_ratio, height_ratio)

            # 计算新尺寸
            new_width = int(original_width * scale_ratio)
            new_height = int(original_height * scale_ratio)

            # 调整尺寸
            img_resized = img.resize((new_width, new_height), Image.Resampling.LANCZOS)

            # 创建目标尺寸背景
            final_img = Image.new('RGB', (target_width, target_height), (255, 255, 255))

            # 居中粘贴
            x_offset = (target_width - new_width) // 2
            y_offset = (target_height - new_height) // 2
            final_img.paste(img_resized, (x_offset, y_offset))

            # 保存
            final_img.save(image_path, 'JPEG', quality=quality, optimize=True)

            compressed_size = os.path.getsize(image_path) / 1024
            reduction = original_size - compressed_size
            reduction_percent = (reduction / original_size) * 100

            print(f"   压缩后: {target_width}×{target_height}px, {compressed_size:.1f}KB")
            print(f"   节省: {reduction:.1f}KB ({reduction_percent:.1f}%)")
            print()

            return True, compressed_size, reduction_percent

    except Exception as e:
        print(f"❌ 失败: {e}")
        return False, 0, 0

def main():
    print("🔧 修复图片压缩问题")
    print("=" * 50)

    # 检查PIL
    if not install_pil_if_needed():
        return

    # 恢复备份文件
    if not restore_backup_images():
        return

    # 检查恢复结果
    images_dir = Path("images")
    image_files = list(images_dir.glob("banner*.jpg"))

    if not image_files:
        print("❌ 没有找到banner图片文件")
        return

    print(f"📋 找到 {len(image_files)} 张图片")
    print()

    # 压缩图片
    success_count = 0
    total_original = 0
    total_compressed = 0

    for image_path in image_files:
        original_size = os.path.getsize(image_path) / 1024

        success, compressed_size, reduction = compress_image_file(image_path)

        if success:
            success_count += 1
            total_original += original_size
            total_compressed += compressed_size

        print("-" * 30)

    # 总结
    print("🎉 压缩完成!")
    print(f"✅ 成功处理: {success_count}/{len(image_files)} 张")
    print(f"📊 总大小: {total_original:.1f}KB → {total_compressed:.1f}KB")

    if total_original > 0:
        total_reduction = total_original - total_compressed
        reduction_percent = (total_reduction / total_original) * 100
        print(f"💾 节省: {total_reduction:.1f}KB ({reduction_percent:.1f}%)")

    print("\n✨ 现在可以重新编译微信小程序，检查包大小!")

if __name__ == "__main__":
    main()