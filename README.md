# 青青社区便民服务平台

## 项目简介

青青社区便民服务平台是一个为社区居民提供便捷生活服务的综合管理系统，包含**后台信息管理子系统**和**前台小程序子系统**。

### 功能模块

| 模块 | 功能 |
|------|------|
| **跑腿服务** | 发布跑腿需求、代买代送、接单管理 |
| **检修服务** | 发布检修需求、水电维修、家电检修 |
| **二手交易** | 发布二手商品、浏览购买、订单管理 |
| **社区论坛** | 发帖交流、评论互动、资讯分享 |
| **用户管理** | 注册登录、个人信息、订单查看 |
| **后台管理** | 数据统计、商品审核、订单管理、用户管理 |

### AI 智能功能

- **需求智能分类**: 用户发布需求时，AI自动提取服务类型、紧急程度和技能标签
- **商品智能估价**: 用户发布二手商品时，AI给出合理估价区间并优化商品描述

## 技术栈

| 层级 | 技术 |
|------|------|
| **前端** | Vue 3 + Element Plus + Vite |
| **后端** | Spring Boot 3.2 + MyBatis Plus |
| **数据库** | MySQL 8.0 |
| **安全** | Spring Security + JWT |
| **AI** | OpenAI兼容API（可替换为通义千问、文心一言等） |

## 项目结构

```
cooperation/
├── database/           # 数据库脚本
│   └── schema.sql
├── backend/           # Spring Boot 后端
│   ├── pom.xml
│   └── src/main/java/com/community/
│       ├── entity/    # 实体类
│       ├── repository/ # Mapper接口
│       ├── service/   # 业务逻辑
│       ├── controller/ # 控制器
│       ├── dto/       # 数据传输对象
│       └── config/    # 配置类
└── frontend/          # Vue 前端
    ├── package.json
    └── src/
        ├── views/     # 页面组件
        ├── api/       # API请求
        ├── router/    # 路由配置
        └── store/     # 状态管理
```

## 快速开始

### 1. 数据库初始化

```bash
# 登录MySQL
mysql -u root -p

# 执行建表脚本
source database/schema.sql
```

### 2. 启动后端

```bash
cd backend

# 修改数据库配置 (application.yml)
# spring.datasource.url=jdbc:mysql://localhost:3306/community_platform
# spring.datasource.username=root
# spring.datasource.password=your_password

# 编译运行
mvn spring-boot:run
```

后端启动后访问: http://localhost:8080/doc.html (API文档)

### 3. 启动前端

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端启动后访问: http://localhost:3000

### 4. 配置AI服务 (可选)

在 `backend/src/main/resources/application.yml` 中配置AI API:

```yaml
ai:
  api-key: your-api-key
  api-url: https://api.openai.com/v1/chat/completions
  model: gpt-3.5-turbo
```

支持的AI服务:
- OpenAI GPT
- 通义千问 (阿里云)
- 文心一言 (百度)
- 智谱ChatGLM

## 默认账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 超级管理员 | admin | admin123 |
| 普通用户 | zhangsan | admin123 |

## API接口

### 认证接口
- `POST /api/auth/login` - 登录
- `POST /api/auth/register` - 注册

### 用户接口
- `GET /api/user/profile` - 获取个人信息
- `PUT /api/user/profile` - 更新个人信息

### 商品接口
- `GET /api/goods` - 商品列表
- `POST /api/goods` - 发布商品

### 需求接口
- `GET /api/repair-requests` - 检修需求列表
- `POST /api/repair-requests` - 发布检修需求
- `GET /api/errand-requests` - 跑腿需求列表
- `POST /api/errand-requests` - 发布跑腿需求

### AI接口
- `POST /api/ai/classify` - AI需求分类
- `POST /api/ai/estimate-price` - AI商品估价

### 管理员接口
- `GET /api/admin/users` - 用户管理
- `GET /api/admin/goods` - 商品管理
- `GET /api/admin/repair` - 检修管理
- `GET /api/admin/errand` - 跑腿管理

## 我的负责内容

我负责的模块包括：

- 二手商品后端逻辑
- 个人信息后端逻辑
- 检修服务后端逻辑

## 开发说明

### 后端开发

```bash
cd backend

# 运行测试
mvn test

# 打包
mvn package
```

### 前端开发

```bash
cd frontend

# 开发模式
npm run dev

# 构建生产版本
npm run build
```

## 课程设计要点

1. **前后端分离架构**: Vue + Spring Boot 完全分离
2. **RESTful API设计**: 标准REST接口规范
3. **JWT认证**: 无状态Token认证机制
4. **AI能力集成**: 大模型API调用与Prompt工程
5. **数据库设计**: 规范化关系型数据库设计
6. **响应式前端**: 移动端友好的UI设计
