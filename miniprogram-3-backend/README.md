# 宋家纺织B2B小程序后端API

## 🚀 启动服务

### 1. 编译和启动
```bash
cd C:\Users\Mayn\WeChatProjects\miniprogram-3-backend
mvn clean install
mvn spring-boot:run
```

### 2. 服务地址
- **API基础地址**: http://localhost:8080/api
- **API文档**: http://localhost:8080/api (各接口)

## 📋 API接口列表

### 🏠 首页相关
- `GET /api/home/data` - 获取首页完整数据
- `GET /api/home/basic` - 获取首页基础数据
- `GET /api/banner/list` - 获取轮播图
- `GET /api/category/list` - 获取分类列表

### 📱 用户相关
- `POST /api/user/login` - 微信登录
  - 参数: openid, nickname, avatarUrl
- `GET /api/user/profile/{userId}` - 获取用户信息
- `PUT /api/user/profile/{userId}` - 更新用户信息
  - 参数: phone, companyName, realName, position, industry, city

### 🛍️ 商品相关
- `GET /api/product/list` - 获取商品列表
  - 参数: page, size
- `GET /api/product/detail/{id}` - 获取商品详情
- `GET /api/product/category/{categoryId}` - 根据分类获取商品
- `GET /api/product/search` - 搜索商品
  - 参数: keyword, page, size
- `GET /api/product/hot` - 热门商品
- `GET /api/product/new` - 新品商品
- `GET /api/product/recommended` - 推荐商品

### ❤️ 收藏相关
- `POST /api/favorite/add` - 添加收藏
  - 参数: userId, productId
- `DELETE /api/favorite/remove` - 取消收藏
  - 参数: userId, productId
- `POST /api/favorite/toggle` - 切换收藏状态
  - 参数: userId, productId
- `GET /api/favorite/list/{userId}` - 获取用户收藏列表
  - 参数: userId, page, size
- `GET /api/favorite/check` - 检查收藏状态
  - 参数: userId, productId
- `GET /api/favorite/count/{userId}` - 获取收藏数量

### 💰 询价相关
- `POST /api/inquiry/submit` - 提交询价
  - 参数: userId, productId, quantity, message
- `GET /api/inquiry/list/{userId}` - 获取用户询价记录
  - 参数: userId, page, size
- `GET /api/inquiry/detail/{id}` - 获取询价详情
- `POST /api/inquiry/reply/{id}` - 管理员回复询价
  - 参数: reply

## 🧪 测试API

### 浏览器测试
1. 启动服务后，在浏览器中访问：
   ```
   http://localhost:8080/api/home/data
   ```

2. 测试用户登录：
   ```
   POST http://localhost:8080/api/user/login
   Content-Type: application/x-www-form-urlencoded

   openid=test_openid_123&nickname=张三&avatarUrl=http://example.com/avatar.jpg
   ```

3. 测试获取商品列表：
   ```
   http://localhost:8080/api/product/list?page=0&size=5
   ```

### Postman测试
导入以下集合到Postman：
```json
{
  "info": {
    "name": "宋家纺织API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "获取首页数据",
      "request": {
        "method": "GET",
        "header": [],
        "url": {
          "raw": "{{base_url}}/home/data",
          "host": ["{{base_url}}"]
        }
      }
    },
    {
      "name": "用户登录",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/x-www-form-urlencoded"
          }
        ],
        "body": {
          "mode": "urlencoded",
          "urlencoded": [
            {
              "key": "openid",
              "value": "test_openid_123"
            },
            {
              "key": "nickname",
              "value": "张三"
            },
            {
              "key": "avatarUrl",
              "value": "http://example.com/avatar.jpg"
            }
          ]
        },
        "url": {
          "raw": "{{base_url}}/user/login",
          "host": ["{{base_url}}"]
        }
      }
    }
  ]
}
```

## 📊 响应格式

所有API响应统一格式：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {...},
  "timestamp": 1641234567890
}
```

错误响应：
```json
{
  "code": 500,
  "message": "操作失败",
  "data": null,
  "timestamp": 1641234567890
}
```

## 🔧 环境配置

### 数据库配置
默认连接本地MySQL：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/songjia_textile
    username: root
    password: 123456
```

### 自定义配置
通过环境变量覆盖配置：
- `DB_PASSWORD`: 数据库密码
- `WECHAT_APP_ID`: 微信小程序AppID
- `WECHAT_APP_SECRET`: 微信小程序AppSecret

## 📝 日志查看

启动成功后可以看到：
```
=================================
🚀 宋家纺织B2B后端服务启动成功！
🌐 API服务地址: http://localhost:8080
📖 API文档: http://localhost:8080/api-docs
=================================
```

## 🐛 常见问题

### 编译错误
- 确保Java 17环境
- 检查Maven配置
- 更新依赖

### 连接数据库失败
- 检查MySQL服务是否启动
- 验证数据库连接信息
- 确认数据库和表已创建

### 跨域问题
- 已配置CORS支持所有来源
- 小程序端无需额外处理

## 📞 技术支持

如有问题，请检查：
1. 控制台日志输出
2. 数据库连接状态
3. API响应内容