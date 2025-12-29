#!/usr/bin/env python3
"""
微信小程序Banner图片压缩工具
将图片压缩到最适合小程序显示的尺寸：750x300px
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
        print("或者：")
        print("python -m pip install Pillow")
        return False

def create_backup(image_path):
    """创建图片备份"""
    backup_path = f"{image_path}.backup"
    if not os.path.exists(backup_path):
        os.rename(image_path, backup_path)
        print(f"📦 创建备份: {os.path.basename(backup_path)}")
        return backup_path
    return None

def compress_image(input_path, target_width=750, target_height=300, quality=90):
    """
    压缩图片到指定尺寸，保持比例，居中裁剪
    """
    try:
        from PIL import Image

        # 打开原图
        with Image.open(input_path) as img:
            print(f"📷 处理图片: {os.path.basename(input_path)}")

            # 获取原始尺寸和文件大小
            original_width, original_height = img.size
            original_size = os.path.getsize(input_path) / 1024  # KB

            print(f"   原始尺寸: {original_width} × {original_height}px")
            print(f"   原始大小: {original_size:.1f}KB")

            # 转换为RGB模式
            if img.mode != 'RGB':
                img = img.convert('RGB')

            # 计算缩放比例，保持宽高比
            width_ratio = target_width / original_width
            height_ratio = target_height / original_height

            # 使用较大的缩放比例，确保完全覆盖目标区域
            scale_ratio = max(width_ratio, height_ratio)

            # 计算新尺寸
            new_width = int(original_width * scale_ratio)
            new_height = int(original_height * scale_ratio)

            # 调整尺寸
            img_resized = img.resize((new_width, new_height), Image.Resampling.LANCZOS)

            # 创建目标尺寸的背景图（白色背景）
            final_img = Image.new('RGB', (target_width, target_height), (255, 255, 255))

            # 计算居中位置
            x_offset = (target_width - new_width) // 2
            y_offset = (target_height - new_height) // 2

            # 将调整后的图片粘贴到中心
            final_img.paste(img_resized, (x_offset, y_offset))

            # 保存压缩后的图片
            final_img.save(input_path, 'JPEG', quality=quality, optimize=True)

            # 获取压缩后的大小
            compressed_size = os.path.getsize(input_path) / 1024  # KB
            size_reduction = original_size - compressed_size
            reduction_percent = (size_reduction / original_size) * 100 if original_size > 0 else 0

            print(f"   ✅ 压缩完成!")
            print(f"   新尺寸: {target_width} × {target_height}px")
            print(f"   新大小: {compressed_size:.1f}KB")
            print(f"   节省: {size_reduction:.1f}KB ({reduction_percent:.1f}%)")
            print()

            return True, compressed_size, reduction_percent

    except Exception as e:
        print(f"❌ 压缩失败 {os.path.basename(input_path)}: {e}")
        return False, 0, 0

def main():
    """主函数"""
    print("🚀 微信小程序Banner图片压缩工具")
    print("=" * 50)
    print("目标尺寸: 750 × 300px")
    print("质量: 90% (高质量JPEG)")
    print()

    # 检查PIL库
    if not install_pil_if_needed():
        return

    # 图片目录
    images_dir = Path("images")
    if not images_dir.exists():
        print(f"❌ 找不到目录: {images_dir}")
        print("请确保在项目根目录运行此脚本")
        return

    # 需要压缩的图片列表
    banner_files = [
        "banner1.jpg",
        "banner2.jpg",
        "banner3.jpg",
        "banner4.jpg",
        "banner5.jpg",
        "banner6.jpg"
    ]

    # 统计信息
    success_count = 0
    total_original_size = 0
    total_compressed_size = 0

    print("📋 开始处理图片...")
    print()

    for filename in banner_files:
        image_path = images_dir / filename

        if image_path.exists():
            # 创建备份
            backup_path = create_backup(image_path)

            # 压缩图片
            success, compressed_size, reduction = compress_image(image_path)

            if success:
                success_count += 1
                total_compressed_size += compressed_size

                # 获取原始大小
                if backup_path:
                    original_size = os.path.getsize(backup_path) / 1024
                    total_original_size += original_size
            else:
                print(f"⚠️ 跳过图片: {filename}")
        else:
            print(f"⚠️ 文件不存在: {filename}")
        print("-" * 40)

    # 输出总结
    print("🎉 压缩完成!")
    print(f"✅ 成功处理: {success_count}/{len(banner_files)} 张图片")
    print()
    print("📊 总体统计:")
    print(f"   原始总大小: {total_original_size:.1f}KB")
    print(f"   压缩总大小: {total_compressed_size:.1f}KB")
    if total_original_size > 0:
        total_reduction = total_original_size - total_compressed_size
        total_reduction_percent = (total_reduction / total_original_size) * 100
        print(f"   总共节省: {total_reduction:.1f}KB ({total_reduction_percent:.1f}%)")
    print()
    print("💡 备份文件: *.backup (如果需要恢复原图)")
    print("📱 现在可以重新编译小程序，检查包大小!")

    # 检查是否还有其他大文件
    print("\n🔍 检查项目中其他可能的大文件...")
    project_size = 0
    for root, dirs, files in os.walk("."):
        # 跳过隐藏目录和node_modules
        dirs[:] = [d for d in dirs if not d.startswith('.') and d != 'node_modules']

        for file in files:
            if not file.startswith('.') and not file.endswith('.backup'):
                file_path = os.path.join(root, file)
                file_size = os.path.getsize(file_path) / 1024
                if file_size > 100:  # 大于100KB的文件
                    print(f"⚠️ 大文件: {file_path} ({file_size:.1f}KB)")
                project_size += file_size

    print(f"\n📦 项目总大小: {project_size:.1f}KB")

if __name__ == "__main__":
    main()