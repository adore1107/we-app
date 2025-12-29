# TDesign 字体加载错误修复指南

## 问题描述
```
[渲染层网络层错误] Failed to load font https://cdn3.codesign.qq.com/icons/gqxWyZ1yMJZmVXk/Yyg5Zp2LG8292lK/iconfont.woff?t=cfc62dd36011e60805f5c3ad1a20b642
net::ERR_CACHE_MISS
```

## 解决方案

### 1. 检查网络连接（最常见原因）
- 确保网络连接正常
- 尝试切换网络（WiFi ↔ 4G/5G）
- 检查是否有网络代理或防火墙限制

### 2. 清理缓存并重新构建
```bash
# 在项目根目录执行
npm run build:weapp
# 或者使用微信开发者工具的"清理缓存"功能
```

### 3. 检查 TDesign 版本
```bash
npm list tdesign-miniprogram
```

如果版本过旧，可以升级：
```bash
npm install tdesign-miniprogram@latest
```

### 4. 配置本地字体（推荐）
在 app.json 中添加字体配置：
```json
{
  "style": "v2",
  "lazyCodeLoading": "requiredComponents",
  "useExtendedLib": {
    "weui": true
  }
}
```

### 5. 临时解决方案 - 忽略错误
这个错误不会影响小程序功能，图标会显示为默认样式。

## 验证方法
1. 重新启动微信开发者工具
2. 清理项目缓存
3. 重新编译运行
4. 检查图标是否正常显示

## 注意事项
- 这个错误通常是网络问题导致的
- 图标显示失败不影响业务功能
- 可以继续正常开发和测试