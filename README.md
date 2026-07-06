# cooperation

## 项目说明

本仓库用于课程设计团队协作过程记录，基于 `lab0901` 的 SpringBoot + SSM + Vue 项目逐步完善社区便民服务平台相关功能。

当前仓库重点体现“协作开发 + 多次提交修复”的过程，不直接放入个人完善版 `community-platform1` 的最终完整代码，而是按课程设计要求分模块逐步迁移、提交、修正。

## 我的负责内容

我负责的模块包括：

- 二手商品后端逻辑
- 个人信息后端逻辑
- 检修服务后端逻辑

本阶段优先完成个人信息后端逻辑。

## 技术栈

- 后端：SpringBoot 2.7.18、MyBatis、MySQL
- 前端：Vue3、Vite、Axios
- 版本管理：Git + GitHub

## 项目结构

```text
lab0901/
├── springboot-ssm-example/   # 后端项目
│   ├── src/main/java/com/example/
│   │   ├── controller/       # 控制层
│   │   ├── service/          # 业务层
│   │   ├── mapper/           # 数据访问层
│   │   └── entity/           # 实体类
│   └── src/main/resources/
│       └── mapper/           # MyBatis XML
└── vue-demo/                 # 前端项目，本阶段暂不修改
```

## 协作提交记录规划

为体现真实开发过程，每个模块会保留至少两类提交：

1. 初始实现提交：先提交可运行但存在明显业务缺口的版本。
2. 修复完善提交：根据问题补齐校验、权限边界和业务闭环。

提交信息会标明 `initial`、`fix`、`完善` 等关键词，方便老师查看迭代过程。

## 当前进度

- 已建立合作仓库说明。
- 下一步：提交个人信息后端逻辑的初始版本，再提交修复完善版本。

## 运行说明

后端项目目录：

```bash
cd springboot-ssm-example
```

使用 Maven Wrapper 启动或测试：

```bash
./mvnw spring-boot:run
```

Windows PowerShell：

```powershell
.\mvnw.cmd spring-boot:run
```

数据库初始化脚本：

```text
springboot-ssm-example/database.sql
```
