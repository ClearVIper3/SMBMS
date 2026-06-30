# 数据库迁移操作手册（Flyway）

> 配合阶段 1 引入。所有数据库结构变更**必须通过新增 `Vx__xxx.sql` 完成**，禁止手工 ALTER。

## 一、目录结构

```
src/main/resources/db/migration/
  V1__init_schema.sql     基线脚本（幂等：CREATE TABLE IF NOT EXISTS + 外键存在性判断）
  R__seed_data.sql        可重复种子数据脚本（ON DUPLICATE KEY UPDATE 幂等）
  V2__xxx.sql             后续结构变更（命名：V<版本号>__<描述>.sql）
  V3__xxx.sql
  ...
```

约定：
- `Vx__` 版本脚本：**只跑一次**，跑过后内容**不允许再改**（改了 checksum 会启动失败）；
- `R__` 可重复脚本：内容变化时自动重跑（用于视图、存储过程、种子数据等幂等内容）；
- 版本号严格递增；多人协作约定通过 PR 协调避免冲突。

## 二、三种数据库环境的接入方式

### 场景 1：全新空库（最常见，本地开发 / 测试 / 全新部署）
直接启动应用，Flyway 会：
1. 创建 `flyway_schema_history` 元数据表；
2. 执行 `V1__init_schema.sql` 建表 + 加外键；
3. 执行 `R__seed_data.sql` 灌种子数据；
4. 后续 V2/V3... 依序应用。

### 场景 2：现网已有数据库（关键：不丢数据）
现网库通常已经存在所有表和真实业务数据，且**没有** `flyway_schema_history`。

`application.yaml` 已配置：
```yaml
spring.flyway.baseline-on-migrate: true
spring.flyway.baseline-version: 1
```

启动行为：
1. Flyway 发现库非空且无元数据表 → 创建 `flyway_schema_history` 并写入一条 "V1 已应用" 的 baseline 记录；
2. **不执行 V1__init_schema.sql**（baseline-version=1 表示 V1 视为已完成）；
3. 执行 `R__seed_data.sql`，因为种子记录的主键已存在，`ON DUPLICATE KEY UPDATE` 只会更新非密码字段（密码不在 UPDATE 列表里，已加密的密码不会被覆盖）；
4. 后续从 V2 开始按需执行。

> 即使 R__seed_data.sql 因为内容变化触发了重跑，也**不会丢数据**：所有 INSERT 都用 ON DUPLICATE KEY UPDATE 包裹，且故意不更新 `userPassword`。

### 场景 3：已用 `data.sql` 手工初始化过、但库与 V1 结构不一致
建议手工对齐到 V1 结构后，按场景 2 接入。具体差异检查：
```sql
SHOW CREATE TABLE smbms_user;   -- 与 V1__init_schema.sql 中 DDL 对比
-- 若有差异，手工补齐索引/外键/字段
```

## 三、新增一次结构变更的标准流程

举例：给 `smbms_user` 加一列 `email`。

1. 新建文件 `V2__user_add_email.sql`：
   ```sql
   ALTER TABLE `smbms_user`
       ADD COLUMN `email` VARCHAR(60) DEFAULT NULL COMMENT '邮箱' AFTER `phone`;
   ```
2. 启动应用 → Flyway 自动执行 V2。
3. 写一份回滚脚本放在 PR 描述里（如 `ALTER TABLE smbms_user DROP COLUMN email`），不放入 db/migration（Flyway 社区版不支持自动回滚），以备人工应急。
4. 若同时要改实体类，**先发数据库变更上线，再发应用上线**，避免新应用代码读到不存在的字段。

## 四、常见坑

| 现象 | 原因 | 解决 |
|---|---|---|
| `FlywayValidateException: ... checksum mismatch` | 已应用的 V1/V2 文件被改过 | 不要改历史脚本；如必须，用 `flyway repair` 或新建 V3 修正 |
| 启动报"Found non-empty schema without schema history table" | 现网库忘记开 `baseline-on-migrate` | 已在 application.yaml 默认开启，无需操作 |
| H2 测试报 Flyway DDL 语法错误 | H2 不识别 MySQL `DELIMITER`/存储过程 | 测试期已通过 `spring.flyway.enabled: false` 关闭 |

## 五、应急工具命令

需要 Flyway CLI（与 pom 中版本一致）：

```powershell
# 查看当前迁移状态
flyway -url="jdbc:mysql://localhost:3306/smbms" -user=root -password=*** info

# 现网首次接入：手工标记 baseline（等价于应用层的 baseline-on-migrate）
flyway -url="jdbc:mysql://localhost:3306/smbms" -user=root -password=*** -baselineVersion=1 baseline

# 失败迁移修复
flyway -url="jdbc:mysql://localhost:3306/smbms" -user=root -password=*** repair
```
