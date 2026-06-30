# SMBMS 超市订单管理系统

现代化重构版：**Spring Boot 3.3 + Java 21 + MyBatis-Plus + Flyway** 的纯 REST 后端 + **Vue 3 + TypeScript + Vite + Element Plus** 的 SPA 前端。

## 技术栈

| 层 | 选型 |
|---|---|
| 运行时 | Java 21 LTS |
| 后端框架 | Spring Boot 3.3.5 |
| 持久层 | MyBatis-Plus 3.5.7 |
| 鉴权 | 自实现最小 JWT（HS256）+ 无状态过滤器 |
| DB | MySQL 8 + Flyway 10 版本管理 |
| 前端 | Vue 3 + TypeScript + Vite + Pinia + Element Plus |
| 测试 | JUnit 5 + H2（MySQL 兼容模式） |
| 部署 | 多阶段 Dockerfile + docker-compose |
| CI | GitHub Actions |

## 目录结构

```
SMBMS/
├─ src/main/java/com/viper/
│   ├─ SmbmsApplication.java        Spring Boot 启动类
│   ├─ config/MvcConfig             JWT 过滤器注册 + CORS
│   ├─ config/PasswordMigrationRunner  明文密码自动升级为 BCrypt
│   ├─ security/                    JWT 服务 / 过滤器 / 上下文
│   ├─ controller/                  REST API + dto/
│   ├─ exception/                   业务异常 + 全局 @RestControllerAdvice
│   ├─ service/  + dao/  + pojo/    业务层 / 持久层 / 实体
│   └─ utils/                       PasswordUtil / Result
├─ src/main/resources/
│   ├─ application.yaml             配置（敏感项 ${ENV:default}）
│   ├─ com/viper/dao/**/*.xml       MyBatis XML
│   └─ db/migration/                Flyway 脚本（V1__ 基线 + R__ 种子）
├─ src/test/                        JUnit5 全套回归测试
├─ frontend/                        Vue 3 + TS 前端工程
├─ Dockerfile                       后端镜像
├─ docker-compose.yml               app + mysql + web
└─ .github/workflows/ci.yml         CI
```

## 本地启动

### 选项 A：一键起完整环境（推荐）

需要 Docker Desktop：

```powershell
docker compose up -d --build
# 前端：http://localhost
# API ：http://localhost:8080/smbms/api
```

### 选项 B：分别启动

**后端**（需要 JDK 21、本地 MySQL 8）：

```powershell
# 1) 复制 .env.example 设置 SMBMS_DB_* 与 SMBMS_JWT_SECRET
# 2) 启动应用，Flyway 会自动建表
mvn spring-boot:run
```

**前端**：

```powershell
cd frontend
npm install
npm run dev
# http://localhost:5173 （/api 自动代理到后端）
```

## 默认账号

- 用户名 `admin`，密码 `123456`
- 启动时 `PasswordMigrationRunner` 会把明文密码自动升级为 BCrypt 哈希；之后数据库中不再存明文。

## 文档索引

- `MIGRATION.md` — 现代化重构看板 + 新旧架构对比 + 后续优化建议
- `MIGRATION_DB.md` — Flyway 在不同库环境下的接入手册
- `frontend/README.md` — 前端开发与构建说明
- `.env.example` — 环境变量模板
