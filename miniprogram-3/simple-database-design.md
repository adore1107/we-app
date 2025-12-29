# 宋家纺织小程序 - 简化数据库设计

## 🎯 设计理念
- **简单实用**：避免过度设计，满足展示需求
- **用户体验**：减少填写信息，提高转化率
- **快速上手**：功能明确，操作简单

---

## 📊 核心表结构

### 1. 用户信息表 (users)
```sql
CREATE TABLE users (
  id INT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
  openid VARCHAR(100) UNIQUE NOT NULL COMMENT '微信openid',
  nickname VARCHAR(50) COMMENT '昵称',
  avatar_url VARCHAR(500) COMMENT '头像URL',
  phone VARCHAR(20) COMMENT '手机号',
  company_name VARCHAR(100) COMMENT '公司名称',
  real_name VARCHAR(50) COMMENT '真实姓名',
  position VARCHAR(50) COMMENT '职位',
  industry VARCHAR(100) COMMENT '所属行业',
  city VARCHAR(50) COMMENT '所在城市',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  last_login_at TIMESTAMP NULL COMMENT '最后登录时间',
  INDEX idx_openid (openid),
  INDEX idx_phone (phone),
  INDEX idx_city (city)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信息表';
```

### 2. 商品分类表 (categories)
```sql
CREATE TABLE categories (
  id INT AUTO_INCREMENT PRIMARY KEY COMMENT '分类ID',
  name VARCHAR(50) NOT NULL COMMENT '分类名称',
  icon VARCHAR(500) COMMENT '分类图标',
  sort_order INT DEFAULT 0 COMMENT '排序',
  status TINYINT DEFAULT 1 COMMENT '状态 1-显示 0-隐藏',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_sort (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';
```

### 3. 商品表 (products)
```sql
CREATE TABLE products (
  id INT AUTO_INCREMENT PRIMARY KEY COMMENT '商品ID',
  name VARCHAR(200) NOT NULL COMMENT '商品名称',
  category_id INT DEFAULT NULL COMMENT '分类ID',
  main_image VARCHAR(500) NOT NULL COMMENT '主图',
  images TEXT COMMENT '商品图片(JSON数组)',
  description TEXT COMMENT '商品描述',
  features TEXT COMMENT '产品特点(JSON数组)',
  specifications TEXT COMMENT '规格参数(JSON数组)',
  min_order_quantity INT DEFAULT 1 COMMENT '最小起订量',
  unit VARCHAR(10) DEFAULT '件' COMMENT '单位',
  lead_time INT DEFAULT 7 COMMENT '交货周期(天)',
  wholesale_price DECIMAL(10,2) COMMENT '批发价',
  retail_price DECIMAL(10,2) COMMENT '零售价',
  sort_order INT DEFAULT 0 COMMENT '排序权重',
  is_hot TINYINT DEFAULT 0 COMMENT '是否热门',
  is_new TINYINT DEFAULT 0 COMMENT '是否新品',
  is_recommended TINYINT DEFAULT 0 COMMENT '是否推荐',
  status TINYINT DEFAULT 1 COMMENT '状态 1-上架 0-下架',
  view_count INT DEFAULT 0 COMMENT '浏览次数',
  favorite_count INT DEFAULT 0 COMMENT '收藏次数',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_category_id (category_id),
  INDEX idx_status (status),
  INDEX idx_sort (sort_order DESC),
  INDEX idx_hot (is_hot),
  INDEX idx_recommended (is_recommended)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';
```

### 4. 用户收藏表 (favorites)
```sql
CREATE TABLE favorites (
  id INT AUTO_INCREMENT PRIMARY KEY COMMENT '收藏ID',
  user_id INT NOT NULL COMMENT '用户ID',
  product_id INT NOT NULL COMMENT '商品ID',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (product_id) REFERENCES products(id),
  UNIQUE KEY uk_user_product (user_id, product_id),
  INDEX idx_user_id (user_id),
  INDEX idx_created_at (created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收藏表';
```

### 5. 询价记录表 (inquiries)
```sql
CREATE TABLE inquiries (
  id INT AUTO_INCREMENT PRIMARY KEY COMMENT '询价ID',
  user_id INT NOT NULL COMMENT '用户ID',
  product_id INT NOT NULL COMMENT '商品ID',
  name VARCHAR(50) COMMENT '联系人姓名',
  phone VARCHAR(20) COMMENT '联系电话',
  company_name VARCHAR(100) COMMENT '公司名称',
  quantity INT COMMENT '询价数量',
  message TEXT COMMENT '留言信息',
  status TINYINT DEFAULT 0 COMMENT '状态 0-待处理 1-已处理',
  reply_content TEXT COMMENT '回复内容',
  reply_time TIMESTAMP NULL COMMENT '回复时间',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (product_id) REFERENCES products(id),
  INDEX idx_user_id (user_id),
  INDEX idx_product_id (product_id),
  INDEX idx_status (status),
  INDEX idx_created_at (created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='询价记录表';
```

### 6. 轮播图表 (banners)
```sql
CREATE TABLE banners (
  id INT AUTO_INCREMENT PRIMARY KEY COMMENT '轮播图ID',
  title VARCHAR(100) COMMENT '标题',
  image_url VARCHAR(500) NOT NULL COMMENT '图片URL',
  link_url VARCHAR(500) COMMENT '跳转链接',
  link_type ENUM('none', 'product', 'category', 'url') DEFAULT 'none' COMMENT '链接类型',
  sort_order INT DEFAULT 0 COMMENT '排序',
  status TINYINT DEFAULT 1 COMMENT '状态 1-显示 0-隐藏',
  start_time TIMESTAMP NULL COMMENT '开始时间',
  end_time TIMESTAMP NULL COMMENT '结束时间',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_sort (sort_order),
  INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='轮播图表';
```

---

## 🗂️ 初始化数据

### 商品分类
```sql
INSERT INTO categories (name, sort_order) VALUES
('凝胶床垫系列', 1),
('天丝床品系列', 2),
('科技布沙发系列', 3),
('冰丝凉席系列', 4),
('功能性面料', 5);
```

### 床上用品示例商品
```sql
-- 凝胶床垫系列
INSERT INTO products (name, category_id, main_image, images, description, min_order_quantity, wholesale_price, retail_price, is_hot, specifications) VALUES
('凝胶恒温床垫面料 Q-max≥0.2', 1,
'https://gw.alicdn.com/bao/uploaded/i1/92042735/O1CN01hcv6GQ1W4gJcj1fEU_!!92042735.jpg_.webp',
JSON_ARRAY(
  'https://gw.alicdn.com/bao/uploaded/i1/92042735/O1CN01hcv6GQ1W4gJcj1fEU_!!92042735.jpg_.webp',
  'http://img.alicdn.com/img/i2/120665841/O1CN01fcm6PC1t1E5dI96u1_!!0-saturn_solar.jpg',
  'http://img.alicdn.com/img/i3/764480013/O1CN01Ta0jso1By0F9Vkntn_!!4611686018427382285-2-saturn_solar.png'
),
'采用先进凝胶技术，持续恒温25℃，提供极致舒适的睡眠体验。智能调温纤维，夏季凉爽，冬季保暖。',
100, 85.00, 120.00, 1,
JSON_OBJECT('材质', '凝胶纤维+棉', 'Q-max值', '≥0.2', '功能', '恒温透气', '适用', '床垫、床品'));

-- 天丝床品系列
INSERT INTO products (name, category_id, main_image, images, description, min_order_quantity, wholesale_price, retail_price, is_new, specifications) VALUES
('80支天丝四件套 高支高密', 2,
'http://img.alicdn.com/img/i2/109580487/O1CN01J1edho1FT5vWoWpJN_!!4611686018427384007-0-saturn_solar.jpg',
JSON_ARRAY(
  'http://img.alicdn.com/img/i2/109580487/O1CN01J1edho1FT5vWoWpJN_!!4611686018427384007-0-saturn_solar.jpg',
  'http://img.alicdn.com/img/i2/2354290166/O1CN01y7BDMY1D64mAW9ifQ_!!4611686018427387382-0-saturn_solar.jpg'
),
'精选优质天丝纤维，80支高支高密，触感丝滑，吸湿透气。环保材质，柔软亲肤，适合高品质酒店使用。',
200, 380.00, 550.00, 1,
JSON_OBJECT('包含', '床单+被套+枕套×2', '材质', '100%天丝', '支数', '80支', '工艺', '高支高密织造'));

-- 科技布沙发系列
INSERT INTO products (name, category_id, main_image, images, description, min_order_quantity, wholesale_price, retail_price, is_recommended, specifications) VALUES
('耐磨科技布沙发面料 10万次耐磨', 3,
'http://img.alicdn.com/img/i2/12913813/O1CN01DgPV8l1e2PJ0Co9wQ_!!4611686018427382933-0-saturn_solar.jpg',
JSON_ARRAY(
  'http://img.alicdn.com/img/i2/12913813/O1CN01DgPV8l1e2PJ0Co9wQ_!!4611686018427382933-0-saturn_solar.jpg',
  'http://img.alicdn.com/img/i1/127659653/O1CN01RtYm032LB8JMPaJc0_!!4611686018427383429-0-saturn_solar.jpg'
),
'高强耐磨科技布，10万次耐磨测试，防水防污，适合现代家居。多种颜色可选，易清洁打理。',
50, 120.00, 180.00, 1,
JSON_OBJECT('耐磨次数', '10万次', '防水等级', 'IPX4级', '材质', '科技布', '颜色', '多色可选'));

-- 冰丝凉席系列
INSERT INTO products (name, category_id, main_image, images, description, min_order_quantity, wholesale_price, retail_price, features) VALUES
('冰丝凉席 恒温25℃', 4,
'http://img.alicdn.com/img/i3/13024373/O1CN01SsnFTl1iAt5nPICgC_!!4611686018427386997-0-saturn_solar.jpg',
JSON_ARRAY(
  'http://img.alicdn.com/img/i3/13024373/O1CN01SsnFTl1iAt5nPICgC_!!4611686018427386997-0-saturn_solar.jpg',
  'http://img.alicdn.com/img/i4/131721392/O1CN01Bgk0tW1M9aNBLr2Vt_!!4611686018427381936-0-saturn_solar.jpg'
),
'天然冰丝材质，恒温25℃，清凉不黏腻，夏日必备。柔软亲肤，机洗可洗，多种尺寸规格。',
150, 68.00, 95.00, 0,
JSON_ARRAY('恒温25℃', '天然冰丝材质', '机洗可洗', '多尺寸可选', '清凉透气'));

-- 功能面料系列
INSERT INTO products (name, category_id, main_image, images, description, min_order_quantity, wholesale_price, retail_price, is_new, specifications) VALUES
('功能性家纺面料 多功能复合', 5,
'http://img.alicdn.com/img/i1/44116660/O1CN01BX288I1z4KOHeL0K0_!!0-saturn_solar.jpg',
JSON_ARRAY(
  'http://img.alicdn.com/img/i1/44116660/O1CN01BX288I1z4KOHeL0K0_!!0-saturn_solar.jpg',
  'http://img.alicdn.com/img/i2/482600029/O1CN01Edwj1l1C5KWeJ41CX_!!4611686018427380829-0-saturn_solar.jpg'
),
'多功能复合面料，集抗菌、防螨、阻燃等功能于一体。适合高端酒店、医疗机构等专业场所。',
80, 95.00, 138.00, 1,
JSON_OBJECT('功能', '抗菌+防螨+阻燃', '材质', '复合纤维', '密度', '高密度', '适用', '酒店、医疗'));
```

### 轮播图数据
```sql
INSERT INTO banners (title, image_url, link_type, sort_order) VALUES
('凝胶恒温床垫技术', '/images/banner1.jpg', 'none', 1),
('80支天丝床品系列', '/images/banner2.jpg', 'category', 2),
('科技布沙发面料', '/images/banner3.jpg', 'product', 3),
('冰丝凉席新品上市', '/images/banner4.jpg', 'none', 4),
('企业批量采购优惠', '/images/banner5.jpg', 'url', 5);
```

---

## 🛠️ API接口设计

### 用户相关
```
POST /api/auth/login        # 微信登录
GET  /api/user/profile      # 获取用户信息
PUT  /api/user/profile      # 更新用户信息
```

### 商品相关
```
GET  /api/products          # 获取商品列表
GET  /api/products/:id      # 获取商品详情
GET  /api/categories        # 获取分类列表
GET  /api/banners           # 获取轮播图
POST /api/products/view     # 增加浏览量
```

### 收藏相关
```
POST /api/favorites        # 添加收藏
GET  /api/favorites        # 获取收藏列表
DELETE /api/favorites/:id   # 取消收藏
```

### 询价相关
```
POST /api/inquiries        # 提交询价
GET  /api/inquiries        # 获取询价记录
```

---

## 🎨 小程序端数据结构

### 用户信息存储
```javascript
// localStorage存储格式
{
  "userInfo": {
    "openid": "xxx",
    "nickname": "张三",
    "avatar_url": "xxx",
    "company_name": "XX纺织贸易公司",
    "real_name": "张经理"
  }
}
```

### 商品数据格式
```javascript
{
  "id": 1,
  "name": "凝胶恒温床垫面料",
  "main_image": "/images/product1.jpg",
  "wholesale_price": 85.00,
  "min_order_quantity": 100,
  "specifications": {
    "材质": "凝胶纤维+棉",
    "功能": "恒温透气"
  },
  "features": ["恒温25℃", "吸湿透气", "机洗可洗"]
}
```

---

## 🚀 部署建议

### 1. 后端技术栈
- **Node.js + Express** (简单快速)
- **MySQL 8.0** (免费稳定)
- **Redis** (可选，用于缓存)

### 2. 小程序端
- 继续使用当前TDesign组件库
- 修改API调用指向新后端
- 保留现有的UI设计

### 3. 开发优先级
1. **用户登录** - 微信授权
2. **商品展示** - 替换mock数据
3. **收藏功能** - 同步到数据库
4. **询价功能** - 表单提交
5. **管理后台** - 简单的数据管理

---

## 💡 用户体验优化

### 1. 简化注册
- 微信一键授权登录
- 只收集必要信息（姓名、电话、公司）
- 后续可补充详细信息

### 2. 快速询价
- 点击即可询价，自动填充已收集信息
- 表单字段最少化
- 提交后显示客服联系方式

### 3. 收藏管理
- 一键收藏/取消收藏
- 收藏列表批量询价
- 收藏商品价格实时更新

这样的设计既保持了简洁性，又满足了B2B展示的核心需求。您觉得这个简化版本怎么样？