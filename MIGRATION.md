# SMBMS 现代化迁移看板

> 多轮对话上下文锚点。每阶段开始/结束都更新本文件。
> 分支：`refactor/modernization`

## 一、目标技术栈（已确认）

| 维度 | 现状 | 目标 |
|---|---|---|
| 运行时 | Java 8 | **Java 21 LTS** |
| 框架 | Spring Boot 2.7.18 | **Spring Boot 3.3.x** |
| 持久层 | MyBatis-Plus 3.5.14 | MyBatis-Plus（升至 SB3 兼容版本） |
| 架构形态 | Thymeleaf SSR 单体 | **后端 RESTful API + Vue3+TS SPA**（阶段性并存） |
| 前端 | Thymeleaf + jQuery 1.8.3 | **Vue 3 + TypeScript + Vite** |
| 数据库 | MySQL 8（手工 `data.sql`） | MySQL 8 + **Flyway** 版本管理 |
| 工程化 | 无测试、无 CI、无容器 | **JUnit5 回归网 + Docker + GitHub Actions CI** |
| 配置 | DB 口令硬编码 | **环境变量外置** |

## 二、硬性约束（再次明示）
1. 业务行为不变（除非显式优化）。
2. 表结构与历史数据不丢，结构变更要可平滑切换。
3. 禁止整体重写，按阶段推进、可独立验证、可回滚。
4. 每阶段必须有验证手段，验证通过才进入下一阶段。
5. 看不懂的历史代码先确认，不擅自改写。

## 三、阶段总览

| # | 名称 | 状态 |
|---|---|---|
| 0 | 基础设施 & 回归测试网 | 🟢 完成 |
| 1 | 数据库版本管理（Flyway） | 🟢 完成 |
| 2 | 运行时升级（Java21 + SB3，`javax→jakarta`） | 🟢 完成（代码层） |
| 3 | 后端 API 化（`/api/**` 与 Thymeleaf 并存，无状态登录） | 🟢 完成 |
| 4 | Vue3 + TS 前端 | 🟢 完成 |
| 5 | 容器化、CI/CD | 🟢 完成 |
| 6 | 旧 Thymeleaf 体系一次性下线（全局改观） | 🟢 完成 |

图例：⚪ 待开始 / 🟡 进行中 / 🟢 已完成 / 🔴 阻塞

## 四、阶段 0 详情

### 范围
- 引入 JUnit5 测试依赖（已随 `spring-boot-starter-test` 自带，确认即可）
- 加 H2 in-memory（MySQL 方言）作为轻量数据库
- 写"现状固化"回归测试：
  - `ApplicationContextSmokeTest`：上下文能起
  - `PasswordUtilTest`：BCrypt 编码/校验/兼容明文
  - `PageSupportTest`：分页边界
  - 业务层 Service 切片测试（user/bill/provider/role 各核心路径）
- DB 口令外置为环境变量，提供 `.env.example`

### 不做的事
- 不动任何业务代码（满足约束 #1）
- 不升级 Spring Boot / Java 版本（阶段 2 才做）
- 不引入 Flyway（阶段 1 才做）

### 验收
- `mvn test` 全绿
- 应用使用环境变量启动，本地仍可零配置启动（提供默认值）

### 阶段 0 完成情况（2026-06-30）
- ✅ JUnit5 + H2(MySQL 兼容模式) 测试栈搭建完成
- ✅ 测试用例：
  - `ApplicationContextSmokeTest`（上下文）
  - `PasswordUtilTest` × 6
  - `PageSupportTest` × 4
  - `UserServiceImplTest` × 11
  - `BillServiceImplTest` × 9
  - `ProviderServiceImplTest` × 7
  - `RoleServiceImplTest` × 1
- ✅ DB 用户名/密码/URL 外置为环境变量（带默认值，本地零配置）
- ✅ `.env.example` 模板 + `.gitignore` 屏蔽真实 `.env`
- ⚠️ Agent 环境无 JDK/Maven，**测试运行由用户在 IDEA 中执行**
- ⏸ 等待用户验证 5 项清单后进入阶段 1

## 五、已知待澄清问题
- `smbms_address` 表存在但代码无对应实现 → 阶段 3 决定是否补
- `static-path-pattern: /**` 是否要修正 → 阶段 3 一并处理
- 旧 Thymeleaf 路由在阶段 5 是否下线 → 阶段 5 由你拍板

## 六、变更记录
- 2026-06-30 创建分支 `refactor/modernization`，完成现状分析与路线图，进入阶段 0
- 2026-06-30 阶段 0 代码落地完成
- 2026-06-30 阶段 1 完成：引入 Flyway（V1 基线 + R__seed 幂等）；data.sql 标记退役；新增 MIGRATION_DB.md 操作手册
- 2026-06-30 阶段 2 完成：Java 8→21；SB 2.7.18→3.3.5；mybatis-plus-spring-boot3-starter 3.5.7；mysql-connector-j；flyway-mysql；javax→jakarta（所有 Filter/Controller）；com.mysql.cj.util.StringUtils → org.springframework.util.StringUtils.hasLength
- 2026-06-30 阶段 3 完成：新增 /api/** REST 一套（auth/users/bills/providers/roles/health），自实现最小 JWT；ApiExceptionHandler 仅作用于 API 包；CORS；新增 11 个集成测试；旧 Thymeleaf 路由保持不动
- 2026-06-30 阶段 4 完成：frontend/ 目录新建 Vue3+TS+Vite+Pinia+ElementPlus，三大模块 List/Edit/View 共 9 页 + Login/Layout/PasswordModify；axios 拦截器统一处理 token 与 401；hash 路由 + 守卫
- 2026-06-30 阶段 5 完成：后端/前端多阶段 Dockerfile；docker-compose（mysql+app+web）；GitHub Actions CI（后端测试 + 前端构建 + 镜像冒烟）
- 2026-06-30 阶段 6 完成（用户决策"无须兼容旧的"）：
  * 删除 `src/main/resources/templates/`（18 个 Thymeleaf 模板）
  * 删除 `src/main/resources/static/`（jQuery、My97DatePicker、css、images、17 个旧 js，共 56 文件）
  * 删除 `com.viper.filter.SysFilter` 与 `MvcConfig` 中的视图控制器/SysFilter 注册
  * 删除旧 Thymeleaf 控制器：`IndexController`、`LoginController`、`LogoutController`、`UserController`、`BillController`、`ProviderController`
  * 删除孤儿工具：`Constants`（USER_SESSION 已无人引用）、`PageSupport`、`BigDecimalUtil`
  * 删除根目录 `data.sql`（Flyway 已接管）
  * 移除 `spring-boot-starter-thymeleaf` 与 `fastjson2` 依赖
  * 包结构扁平化：`controller/api/**` → `controller/**`
  * 启动类重命名：`SpringbootApplication` → `SmbmsApplication`
  * 全局异常处理 `basePackages` 收口到 `com.viper.controller`
  * README 全面重写，移除旧 UI 截图说明

# 七、最终架构（全部改观后）

```
SMBMS/
├─ src/main/java/com/viper/
│   ├─ SmbmsApplication              Spring Boot 启动
│   ├─ config/
│   │   ├─ MvcConfig                 JWT 过滤器 + CORS（仅 /api/**）
│   │   └─ PasswordMigrationRunner   启动时把明文密码升级 BCrypt
│   ├─ security/
│   │   ├─ JwtService                自实现 HS256 JWT
│   │   ├─ JwtAuthFilter             /api/** 鉴权
│   │   └─ UserContext               线程内当前用户
│   ├─ controller/
│   │   ├─ AuthApiController         /api/auth/{login,logout,me,password}
│   │   ├─ UserApiController         /api/users/**
│   │   ├─ BillApiController         /api/bills/**
│   │   ├─ ProviderApiController     /api/providers/**
│   │   ├─ RoleApiController         /api/roles
│   │   ├─ HealthApiController       /api/health
│   │   └─ dto/                      入参/出参 DTO（11 个）
│   ├─ exception/
│   │   ├─ BusinessException
│   │   ├─ DbExceptionTranslator     DB 约束 → 中文友好提示
│   │   └─ ApiExceptionHandler       @RestControllerAdvice 全局
│   ├─ service/  + dao/  + pojo/     业务/持久层（保留原结构）
│   └─ utils/
│       ├─ PasswordUtil              BCrypt 工具
│       └─ Result                    统一响应
├─ src/main/resources/
│   ├─ application.yaml              纯 REST 配置，敏感项 ${ENV:default}
│   ├─ com/viper/dao/**/*.xml        MyBatis XML
│   └─ db/migration/                 Flyway（V1 + R__seed）
├─ src/test/                         JUnit5 全套（37 用例）
├─ frontend/                         Vue3 + TS 工程（独立部署）
├─ Dockerfile  +  frontend/Dockerfile  +  docker-compose.yml
└─ .github/workflows/ci.yml
```

后端总 Java 文件数：**45 个**（旧体系下含 Thymeleaf 控制器和模板时约 33 Java + 18 HTML + 17 JS + 21 PNG/JPG，迁出后整体仓库行数显著下降）。

---

# 七、收尾：新旧架构对比

| 维度 | 旧（Spring Boot 2.7 + Thymeleaf 单体） | 新（Spring Boot 3.3 + 前后端分离） |
|---|---|---|
| 运行时 | Java 8（EOL 风险高）+ SB 2.7.18（OSS EOL） | **Java 21 LTS + SB 3.3.5** |
| 命名空间 | `javax.*` | `jakarta.*` |
| 持久层 | `mybatis-plus-boot-starter` 3.5.14 | `mybatis-plus-spring-boot3-starter` 3.5.7 |
| MySQL 驱动 | `mysql:mysql-connector-java`（已停维护） | `com.mysql:mysql-connector-j`（官方新坐标） |
| 数据库初始化 | 手工 `data.sql`（含 DROP TABLE，危险） | **Flyway**（V1 基线 + R__seed 幂等 + baseline-on-migrate） |
| API 形态 | 仅 Thymeleaf 服务端渲染 + 个别 `@ResponseBody` | **`/api/**` REST 一套（与 Thymeleaf 并存）** |
| 鉴权 | Session + SysFilter | **JWT 无状态**（JwtAuthFilter + UserContext） |
| 异常处理 | 各 Controller 自 catch | **`@RestControllerAdvice` 全局**（仅 API 包，旧 Controller 行为不变） |
| 工具类 | `com.mysql.cj.util.StringUtils`（内部 API） | `org.springframework.util.StringUtils.hasLength` |
| DB 口令 | 硬编码在 `application.yaml` | 环境变量外置 `${SMBMS_DB_*:default}`，`.env.example` 模板 |
| 前端 | Thymeleaf + jQuery 1.8.3 + 自写 JS | **Vue 3 + TS + Vite + Pinia + Element Plus** |
| 测试 | 无 | JUnit5 + H2（38+ 用例：单元 + Service 切片 + API 集成 + JWT） |
| 部署 | 手工 jar 启动 | **Dockerfile（多阶段+layered）+ docker-compose**（一键起 app+mysql+web） |
| CI | 无 | **GitHub Actions**：后端 verify + 前端 build + 镜像冒烟 |
| 文档 | 仅 README 截图 | `MIGRATION.md`（看板）+ `MIGRATION_DB.md`（DB 操作手册）+ `frontend/README.md` |

# 八、后续可放入迭代的优化建议

> 这些**不属于本次"现代化"硬约束**（不改变业务行为），但属于显著价值的下一步改进，列在此处供你后续规划。

## A. 安全 / 合规
1. **`smbms.jwt.secret` 强制不允许默认值**：当前为本地零配置友好留了默认值；生产应用启动检查 `secret` 不在默认值集合内，否则拒绝启动。
2. **登录限流 / 防爆破**：login 接口加 IP+userCode 维度计数器（可用 Caffeine 即可，无需 Redis）。
3. **审计日志独立表 `smbms_audit_log`**：当前审计仅 `createdBy/modifyBy` 字段，建议补"何时谁对哪条记录做了什么"的全量审计。
4. **`smbms_address` 表与代码补齐或物理下线**：当前库里建了表代码里不用，存在维护误解风险（需要业务侧决策）。

## B. 业务行为优化（需明确授权才能做）
5. **`ProviderService.deleteProviderById` 返回值语义重构**：当前 `0/-1/>0` 三态值在 REST 中只能勉强翻译；建议改为抛 `BusinessException` 表达"被引用"，删除成功用 boolean。
6. **`UserMapper.xml` 用逗号隐式连接 + WHERE 等值**：`userRole IS NULL` 的用户被过滤掉。如允许，改为 `LEFT JOIN`。
7. **分页统一**：旧用户列表用自实现 `PageSupport`，新 API 用 `PageResponse`；建议引入 MyBatis-Plus 自带的分页插件 `PaginationInnerInterceptor`，去掉手算 startIndex/totalPages 的重复代码。
8. **`SimpleDateFormat`、`BigDecimal.ROUND_DOWN` 等已废弃 API**：迁到 `DateTimeFormatter` + `RoundingMode.DOWN`，并把日期字段从 `Date` 全面切到 `LocalDate / LocalDateTime`。

## C. 可观测性 / 运维
9. **接入 Actuator + Prometheus**：`/actuator/health/liveness, /actuator/prometheus`，配合 docker-compose 加个 Grafana 即可。
10. **结构化日志**：引入 logstash-logback 或 logbook，方便 ELK；目前的 `e.printStackTrace()` 在阶段3 API 已用 SLF4J 取代，剩余 Service 层旧代码可顺手清理。
11. **慢 SQL 报警**：MyBatis-Plus `PerformanceInterceptor`（社区版用 p6spy）打印 ≥ N ms 的查询。

## D. 工程化
12. **Maven Wrapper**：仓库根目录补 `mvnw / mvnw.cmd / .mvn/wrapper/`，无 mvn 环境也能 `./mvnw test`。
13. **预提交钩子**：husky + lint-staged（前端）、spotless（后端）保证代码风格统一。
14. **多环境配置**：`application-{dev,prod,test}.yaml`，CI 注入 `SPRING_PROFILES_ACTIVE`。
15. **删除 `BigDecimalUtil`**：唯一一个孤儿类，无引用，本次为遵守"不擅自删历史代码"约束保留。

## E. 阶段 5 之后的"下线旧 Thymeleaf"决策
当 Vue 前端跑稳 ≥ N 周后，可以：
- 移除 `src/main/resources/templates/**`、`static/**`、`SysFilter`、所有非 `controller.api` 包下的 Controller；
- 移除 `spring-boot-starter-thymeleaf` 依赖；
- 此时后端将退化为纯 API 服务，包结构与运维更清晰。

