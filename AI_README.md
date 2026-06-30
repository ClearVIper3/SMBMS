# SMBMS AI 助手模块

让用户用自然语言查询业务数据；AI **只能**通过白名单工具访问数据库，物理上无法执行 INSERT/UPDATE/DELETE/DROP。

## 一、整体架构

```
浏览器 (Vue3 AI 助手页)
   │ fetch + ReadableStream（SSE）
   ▼
Spring Boot 3
   AiChatController ── SSE ──► AiChatService ──► OpenAiStreamingChatModel
                                       │
                                       ├── AiToolRegistry（白名单 @Tool）
                                       │      ├ UserQueryTool      (复用 UserService)
                                       │      ├ ProviderQueryTool  (复用 ProviderService)
                                       │      ├ BillQueryTool      (复用 BillService)
                                       │      ├ InventoryTool      (按订单聚合估库存)
                                       │      └ SalesStatisticsTool(按订单日期聚合)
                                       │
                                       └── ConversationService（DB 持久化）
                                              ├ ai_chat_session
                                              └ ai_chat_message
```

## 二、安全模型

| 风险 | 控制手段 |
|---|---|
| AI 执行任意 SQL | **物理上做不到**：无法访问 `DataSource` / `JdbcTemplate` / `SqlSession`，只能调用 `@Tool` 注解的方法 |
| INSERT/UPDATE/DELETE/DROP | 注册表里只挂查询方法；`UserService.add/modify/delete` 等写方法**不在白名单** |
| 越权读取他人数据 | `ToolGuard.requireLogin()` 每次工具调用前校验 `UserContext`；`ConversationService` 严格按 `user_id` 隔离 |
| 全表扫描 / 大数据下发 | `ToolGuard.clampPageSize` 单次最多 50 行；列表型工具用 `.limit(MAX_PAGE_SIZE)` 兜底 |
| Prompt 注入要求改数据 | 系统提示词明确拒绝；同时模型即便"想"调写工具，也找不到对应 ToolSpecification |
| LLM 死循环工具调用 | `AiChatService.MAX_TOOL_ROUNDS = 5` 强制截断 |
| 未配置 key 启动失败 | `smbms.ai.enabled=false` 时不装配 model；API 返回 503 + 友好提示 |

## 三、Tool Calling 工作流程

```
用户问："最近7天销售额最高的商品是什么？"
   │
   ▼
LLM (qwen-plus) 看到 5 个工具的 ToolSpecification
   │
   ▼ 决定调用
{ name: "getSalesStatistics",
  arguments: { startDate: "2026-06-24", endDate: "2026-06-30" } }
   │
   ▼
SalesStatisticsTool.getSalesStatistics(...)
   │
   ▼ 走 BillService → MyBatis-Plus → MySQL
返回 { totalSales, orderCount, topProducts:[...], topProviders:[...] }
   │
   ▼ 结果作为 ToolExecutionResultMessage 回灌给 LLM
   ▼
LLM 基于真实数据生成最终自然语言回答（流式 token 直接 SSE 推前端）
```

## 四、API 列表

| Method | Path | 说明 |
|---|---|---|
| GET | `/api/ai/status` | AI 是否启用 |
| GET | `/api/ai/sessions` | 列出我的会话 |
| POST | `/api/ai/sessions` | 新建会话 |
| DELETE | `/api/ai/sessions/{id}` | 删除会话 |
| GET | `/api/ai/sessions/{id}/messages` | 历史消息 |
| POST | `/api/ai/sessions/{id}/messages` | **SSE 流式**提问 |
| POST | `/api/ai/sessions/{id}/regenerate` | **SSE 流式**重新生成 |

### SSE 事件类型

```
event: delta        data: "<token 片段>"
event: tool_call    data: {"name":"getSalesStatistics","arguments":{...}}
event: tool_result  data: {"name":"getSalesStatistics","result":"{...}"}
event: error        data: "<错误信息>"
event: done         data: ""
```

## 五、本地启动

1. 在 `.env` 中配置：

   ```
   SMBMS_AI_ENABLED=true
   SMBMS_AI_BASE_URL=https://dashscope.aliyuncs.com/compatible-mode/v1
   SMBMS_AI_API_KEY=sk-xxxxx
   SMBMS_AI_MODEL=qwen-plus
   ```

   切换其它供应商：
   - DeepSeek：`SMBMS_AI_BASE_URL=https://api.deepseek.com/v1`、`SMBMS_AI_MODEL=deepseek-chat`
   - Moonshot：`SMBMS_AI_BASE_URL=https://api.moonshot.cn/v1`、`SMBMS_AI_MODEL=moonshot-v1-8k`
   - OpenAI：`SMBMS_AI_BASE_URL=https://api.openai.com/v1`、`SMBMS_AI_MODEL=gpt-4o-mini`

2. 后端：`mvn spring-boot:run`（启动时 Flyway 自动执行 `V2__ai_chat_tables.sql`）
3. 前端：`cd frontend && npm install && npm run dev`，菜单进入 **🤖 AI 助手**

## 六、扩展新工具的步骤

1. 在 `service/ai/tool/` 新建一个 `@Component`，方法加 `@Tool(name="...", value="给 LLM 看的描述")`，参数用 `@P("说明")`；
2. 在工具入口调用 `ToolGuard.requireLogin()`（与必要的权限校验）；
3. 在 `AiToolRegistry` 构造函数注入并 `register(...)` —— 注册表是白名单的唯一入口，没注册的方法 LLM 永远调不到；
4. 重启即生效。
