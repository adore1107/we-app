# 松佳纺织B2B小程序 - 后台管理系统需求分析

> 生成时间：2025-12-31
> 项目类型：B2B纺织品批发平台
> 技术栈：Spring Boot 3.1.5 + MyBatis-Plus + MySQL 8.0 + 微信小程序

---

## 📊 一、数据库架构分析

### 1.1 核心数据表（8张）

| 表名 | 中文名 | 记录数 | 核心字段 | 管理优先级 |
|------|--------|--------|----------|-----------|
| `products` | 商品表 | ~154 | name, main_image, wholesale_price, status | ⭐⭐⭐⭐⭐ |
| `categories` | 分类表 | ~30 | name, parent_id, level, icon | ⭐⭐⭐⭐⭐ |
| `users` | 用户表 | ~16 | openid, nickname, company_name, phone | ⭐⭐⭐⭐ |
| `banners` | 轮播图表 | ~12 | image_url, type, link_url, sort_order | ⭐⭐⭐⭐ |
| `favorites` | 收藏表 | ~33 | user_id, product_id | ⭐⭐⭐ |
| `comments` | 评论表 | ~24 | content, rating, admin_reply | ⭐⭐⭐⭐ |
| `product_views` | 浏览记录表 | 新增 | user_id, product_id, duration_seconds | ⭐⭐⭐⭐ |
| `inquiries` | 询价表 | 0 | quantity, message, admin_reply | ⭐⭐⭐ |

### 1.2 数据表详细结构

#### 📦 Products 商品表
```sql
核心字段：
- id: 商品ID
- name: 商品名称 ✏️
- main_category_id: 一级分类 ✏️
- sub_category_id: 二级分类（可选） ✏️
- main_image: 主图URL ✏️
- images: 轮播图JSON数组 ✏️
- description: 商品描述 ✏️
- features: 产品特点JSON ✏️
- specifications: 规格参数JSON ✏️
- wholesale_price: 批发价（分） ✏️
- retail_price: 零售价（分） ✏️
- min_order_quantity: 最小起订量 ✏️
- unit: 单位（件/米/吨） ✏️
- lead_time: 交货周期（天） ✏️
- sort_order: 排序权重 ✏️
- is_hot: 是否热门 ✏️
- is_new: 是否新品 ✏️
- is_recommended: 是否推荐 ✏️
- status: 上架状态 ✏️
- view_count: 浏览次数 📊
- favorite_count: 收藏次数 📊
- created_at: 创建时间
- updated_at: 更新时间

B2B特性：
✓ 批发价/零售价双价格体系
✓ 最小起订量限制
✓ 交货周期管理
✓ 规格参数JSON（灵活配置）
```

#### 🏷️ Categories 分类表
```sql
核心字段：
- id: 分类ID
- parent_id: 父分类ID（NULL=一级分类）
- level: 层级（1=一级，2=二级） ✏️
- name: 分类名称 ✏️
- icon: 分类图标URL ✏️
- sort_order: 排序 ✏️
- status: 启用状态 ✏️

树形结构：
一级分类（parent_id=NULL, level=1）
  ├─ 二级分类A（parent_id=1, level=2）
  ├─ 二级分类B（parent_id=1, level=2）
  └─ 二级分类C（parent_id=1, level=2）
```

#### 👥 Users 用户表
```sql
核心字段：
- id: 用户ID
- openid: 微信openid（唯一） 🔐
- nickname: 昵称
- avatar_url: 头像
- phone: 手机号 ✏️
- company_name: 公司名称（B2B） ✏️
- real_name: 真实姓名 ✏️
- position: 职位 ✏️
- industry: 所属行业 ✏️
- city: 所在城市 ✏️
- created_at: 注册时间
- last_login_at: 最后登录

B2B特性：
✓ 企业信息（公司名、行业）
✓ 联系人信息（姓名、职位、电话）
```

#### 🎨 Banners 轮播图表
```sql
核心字段：
- id: 轮播图ID
- type: 类型（home_banner/company_gallery/product_banner） ✏️
- title: 标题 ✏️
- image_url: 图片URL ✏️
- link_url: 跳转链接 ✏️
- link_type: 链接类型（none/product/category/url） ✏️
- sort_order: 排序 ✏️
- status: 启用状态 ✏️
- start_time: 开始时间 ✏️
- end_time: 结束时间 ✏️

支持类型：
✓ home_banner - 首页轮播
✓ company_gallery - 公司风采
✓ product_banner - 商品轮播
```

#### 💬 Comments 评论表
```sql
核心字段：
- id: 评论ID
- user_id: 用户ID
- product_id: 商品ID
- content: 评论内容
- rating: 评分（1-5星）
- admin_reply: 商家回复 ✏️
- reply_time: 回复时间
- status: 显示状态（0隐藏/1显示） ✏️
- deleted: 软删除（0正常/1删除） ✏️

管理功能：
✓ 评论审核（显示/隐藏）
✓ 商家回复
✓ 删除管理
```

#### 📊 Product_Views 浏览记录表（新增）
```sql
核心字段：
- id: 记录ID
- user_id: 用户ID
- product_id: 商品ID
- view_start_time: 进入时间
- view_end_time: 离开时间
- duration_seconds: 浏览时长（秒）
- created_at: 创建时间

数据分析价值：
✓ 商品浏览次数（PV）
✓ 独立访客数（UV）
✓ 平均浏览时长
✓ 用户行为分析
```

#### 📝 Inquiries 询价表
```sql
核心字段：
- id: 询价ID
- user_id: 用户ID
- product_id: 商品ID
- quantity: 询价数量
- message: 留言/特殊要求
- status: 状态（待处理/已回复/已关闭） ✏️
- admin_reply: 客服回复 ✏️
- reply_time: 回复时间

B2B核心功能：
✓ 批量询价
✓ 特殊需求备注
✓ 客服回复管理
```

---

## 🔌 二、后端API分析

### 2.1 现有Controller及接口

#### ProductController（商品管理）
```java
GET  /product/list                    // 商品列表（分页、搜索、分类筛选）
GET  /product/detail/{id}             // 商品详情
GET  /product/category/{id}           // 按分类获取商品
GET  /product/hot                     // 热门商品
GET  /product/new                     // 新品商品
GET  /product/recommended             // 推荐商品
POST /product/view/duration           // 更新浏览时长
```

#### CategoryController（分类管理）
```java
GET /category/list                    // 分类列表
GET /category/tree                    // 分类树形结构
```

#### BannerController（轮播图管理）
```java
GET /banner/list                      // 轮播图列表
```

#### UserController（用户管理）
```java
POST /user/login                      // 用户登录
GET  /user/profile/{id}               // 用户资料
```

#### CommentController（评论管理）
```java
GET  /comment/product/{id}            // 商品评论列表
POST /comment/add                     // 添加评论
```

#### FavoriteController（收藏管理）
```java
GET    /favorite/list                 // 收藏列表
POST   /favorite/add                  // 添加收藏
DELETE /favorite/remove               // 取消收藏
```

#### InquiryController（询价管理）
```java
POST /inquiry/submit                  // 提交询价
GET  /inquiry/list                    // 询价列表
```

### 2.2 后端技术栈
```
✓ Spring Boot 3.1.5
✓ MyBatis-Plus（分页、QueryWrapper）
✓ MySQL 8.0
✓ Lombok（简化代码）
✓ 统一响应格式：Result<T>
✓ 外键约束（CASCADE删除）
✓ 软删除支持（deleted字段）
```

---

## 📱 三、前端小程序功能分析

### 3.1 核心页面
```
├─ 首页 (pages/home)
│  ├─ 轮播图展示
│  ├─ 分类导航
│  ├─ 热门商品
│  └─ 推荐商品
│
├─ 分类 (pages/category)
│  ├─ 一级分类列表
│  ├─ 二级分类展示
│  └─ 商品筛选（综合/最新/热门）
│
├─ 商品详情 (pages/goods/details)
│  ├─ 轮播图
│  ├─ 基本信息（价格、起订量、交货期）
│  ├─ 规格参数
│  ├─ 商品描述
│  ├─ 评论列表
│  ├─ 收藏功能
│  └─ 浏览时长追踪 ✨
│
├─ 搜索 (pages/goods/search & result)
│  ├─ 关键词搜索
│  ├─ 搜索历史
│  ├─ 分类内搜索
│  └─ 排序（综合/最新/热门）✨
│
└─ 个人中心 (pages/usercenter)
   ├─ 用户信息
   ├─ 我的收藏
   └─ 企业信息
```

### 3.2 已实现的核心功能
```
✅ 微信登录（openid）
✅ 商品浏览（支持分类、搜索、排序）
✅ 商品详情查看
✅ 收藏功能
✅ 评论功能
✅ 浏览统计（PV/UV + 时长追踪）
✅ 动态排序（综合/最新/热门）
```

---

## 🎯 四、后台管理系统需求分析

### 4.1 核心管理模块（优先级排序）

#### 🥇 优先级1：商品管理（最核心）
```
功能需求：
✅ 商品列表（分页、搜索、筛选）
  - 按分类筛选
  - 按状态筛选（上架/下架）
  - 按标签筛选（热门/新品/推荐）
  - 搜索（商品名称、ID）

✅ 商品新增/编辑
  - 基本信息（名称、分类、价格）
  - 图片上传（主图 + 轮播图）
  - 详情编辑（富文本/Markdown）
  - 规格参数（动态表单）
  - 产品特点（标签管理）
  - B2B参数（起订量、单位、交货期）
  - 标签设置（热门/新品/推荐）
  - 排序权重
  - 上下架管理

✅ 批量操作
  - 批量上下架
  - 批量删除
  - 批量修改分类
  - 批量设置标签

✅ 数据统计
  - 浏览量排行
  - 收藏量排行
  - 评论统计
```

#### 🥇 优先级1：分类管理
```
功能需求：
✅ 分类列表（树形展示）
  - 一级分类
  - 二级分类（嵌套）

✅ 分类新增/编辑
  - 分类名称
  - 分类图标上传
  - 父分类选择
  - 排序设置
  - 启用/禁用

✅ 拖拽排序
  - 可视化调整分类顺序

✅ 分类统计
  - 每个分类下的商品数量
```

#### 🥈 优先级2：轮播图管理
```
功能需求：
✅ 轮播图列表
  - 按类型分组（首页/公司风采/商品）
  - 拖拽排序

✅ 轮播图新增/编辑
  - 图片上传
  - 标题
  - 链接设置（商品/分类/自定义URL）
  - 类型选择
  - 有效期设置
  - 启用/禁用

✅ 批量操作
  - 批量删除
  - 批量启用/禁用
```

#### 🥈 优先级2：用户管理
```
功能需求：
✅ 用户列表（分页、搜索）
  - 按注册时间筛选
  - 按城市筛选
  - 按行业筛选
  - 搜索（昵称、公司名、手机号）

✅ 用户详情
  - 基本信息
  - 企业信息
  - 注册时间/最后登录
  - 收藏商品列表
  - 浏览记录
  - 询价记录
  - 评论记录

✅ 用户统计
  - 新增用户趋势
  - 活跃用户分析
  - 地域分布
  - 行业分布
```

#### 🥈 优先级2：评论管理
```
功能需求：
✅ 评论列表（分页、筛选）
  - 按商品筛选
  - 按评分筛选
  - 按状态筛选（待审核/已显示/已隐藏）
  - 时间范围筛选

✅ 评论审核
  - 显示/隐藏
  - 删除

✅ 商家回复
  - 回复评论
  - 编辑回复
  - 删除回复

✅ 评论统计
  - 平均评分
  - 评分分布
  - 待回复数量
```

#### 🥉 优先级3：数据分析
```
功能需求：
✅ 商品数据分析
  - 浏览量排行（PV）
  - 独立访客排行（UV）
  - 平均浏览时长
  - 收藏量排行
  - 评论最多的商品

✅ 用户行为分析
  - 浏览时长分布
  - 访问时段分析
  - 热门搜索词
  - 用户留存分析

✅ 趋势分析
  - 每日新增用户
  - 每日活跃用户
  - 商品浏览趋势
  - 收藏趋势

✅ 数据导出
  - Excel导出
  - 自定义日期范围
```

#### 🥉 优先级3：询价管理
```
功能需求：
✅ 询价列表（分页、筛选）
  - 按状态筛选（待处理/已回复/已关闭）
  - 按商品筛选
  - 按用户筛选
  - 时间范围筛选

✅ 询价处理
  - 查看详情（商品、数量、留言）
  - 回复客户
  - 修改状态
  - 标记重要

✅ 询价统计
  - 待处理数量
  - 平均响应时间
  - 转化率分析
```

### 4.2 系统设置模块

#### 管理员管理
```
✅ 管理员账号（独立于users表）
  - 用户名/密码登录
  - 角色权限
  - 操作日志

✅ 角色权限
  - 超级管理员
  - 商品管理员
  - 客服
  - 数据分析员
```

#### 系统配置
```
✅ 基础设置
  - 平台名称
  - LOGO
  - 联系方式

✅ 上传配置
  - 图片存储路径
  - 允许的文件类型
  - 文件大小限制
```

---

## 🎨 五、后台管理系统技术方案

### 5.1 推荐技术栈

#### 方案A：Vue 3 + Element Plus（推荐）
```
前端：
✓ Vue 3 + Vite
✓ Element Plus UI组件库
✓ Vue Router
✓ Pinia状态管理
✓ Axios

优势：
✓ 生态成熟
✓ 组件丰富
✓ 上手快
✓ 社区活跃
```

#### 方案B：React + Ant Design
```
前端：
✓ React 18
✓ Ant Design / Ant Design Pro
✓ React Router
✓ Redux Toolkit
✓ Axios

优势：
✓ 企业级UI
✓ Pro版本开箱即用
✓ 表格功能强大
```

### 5.2 后端扩展需求

#### 新增Controller
```java
AdminProductController     // 商品管理API
AdminCategoryController    // 分类管理API
AdminBannerController      // 轮播图管理API
AdminUserController        // 用户管理API
AdminCommentController     // 评论管理API
AdminInquiryController     // 询价管理API
AdminStatisticsController  // 数据统计API
AdminAuthController        // 管理员登录/权限
```

#### 新增Entity
```java
Admin      // 管理员表
AdminRole  // 角色表
AdminLog   // 操作日志表
```

#### 通用功能
```java
✓ JWT认证
✓ 权限拦截器
✓ 操作日志AOP
✓ 文件上传服务
✓ Excel导出服务
```

---

## 📋 六、开发优先级建议

### Phase 1：核心功能（2-3周）
```
1. 管理员登录/权限系统
2. 商品管理（CRUD + 上传）
3. 分类管理（树形结构）
4. 轮播图管理
```

### Phase 2：内容管理（1-2周）
```
1. 用户管理（查看、搜索）
2. 评论管理（审核、回复）
3. 询价管理（处理、回复）
```

### Phase 3：数据分析（1-2周）
```
1. 商品数据统计
2. 用户行为分析
3. 浏览数据分析
4. Dashboard首页
```

### Phase 4：优化完善（1周）
```
1. 批量操作
2. 数据导出
3. 操作日志
4. 系统设置
```

---

## 🗂️ 七、数据库扩展建议

### 需要新增的表

#### admins 管理员表
```sql
CREATE TABLE `admins` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL UNIQUE COMMENT '用户名',
  `password` varchar(255) NOT NULL COMMENT '密码（加密）',
  `real_name` varchar(50) COMMENT '真实姓名',
  `role` enum('super_admin','product_admin','customer_service','analyst') DEFAULT 'product_admin',
  `status` bit(1) DEFAULT b'1' COMMENT '启用状态',
  `last_login_at` timestamp NULL,
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) COMMENT='管理员表';
```

#### admin_logs 操作日志表
```sql
CREATE TABLE `admin_logs` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int NOT NULL,
  `action` varchar(100) COMMENT '操作类型',
  `module` varchar(50) COMMENT '模块',
  `content` text COMMENT '操作内容',
  `ip` varchar(50) COMMENT 'IP地址',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_admin_id` (`admin_id`),
  KEY `idx_created_at` (`created_at`)
) COMMENT='管理员操作日志表';
```

---

## 📌 八、关键注意事项

### 8.1 B2B特性
```
✓ 价格以分为单位存储（避免浮点误差）
✓ 最小起订量限制
✓ 交货周期管理
✓ 批发价/零售价双价格
✓ 企业信息管理
✓ 询价系统
```

### 8.2 数据完整性
```
✓ 外键约束（CASCADE删除）
✓ 软删除机制（deleted字段）
✓ 唯一索引（防重复）
✓ 状态管理（bit类型）
```

### 8.3 性能优化
```
✓ 已有索引优化（view_count, favorite_count, created_at）
✓ 分页查询（MyBatis-Plus Page）
✓ JSON字段（images, features, specifications）
✓ 缓存策略（分类、轮播图）
```

### 8.4 安全性
```
✓ 管理员JWT认证
✓ 角色权限控制
✓ 操作日志记录
✓ 文件上传校验
✓ SQL注入防护（MyBatis-Plus）
```

---

## 🚀 九、快速启动建议

### 第一步：确定技术栈
```
推荐：Vue 3 + Element Plus + Spring Boot
理由：
- Element Plus组件丰富（表格、表单、上传等）
- 与现有Spring Boot后端无缝对接
- 学习曲线平缓
- 社区资源丰富
```

### 第二步：搭建项目框架
```bash
# 前端
npm create vite@latest admin-panel -- --template vue
cd admin-panel
npm install element-plus axios vue-router pinia

# 后端（已有，只需扩展）
- 创建admin包（controller/service/entity）
- 添加JWT依赖
- 配置跨域CORS
```

### 第三步：实现登录系统
```
1. 创建Admin实体
2. 实现AdminController（登录/登出）
3. JWT工具类
4. 前端登录页面
5. 路由守卫
```

### 第四步：实现核心功能
```
按优先级逐个实现：
1. 商品管理 → 2. 分类管理 → 3. 轮播图管理
```

---

**需要我开始帮你实现后台管理系统吗？请告诉我：**
1. **选择哪个技术栈？**（Vue 3 + Element Plus 或 React + Ant Design）
2. **从哪个模块开始？**（建议从管理员登录系统开始）
3. **是否需要独立的后台项目？**（还是集成到现有的miniprogram-3-backend）
