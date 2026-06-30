package com.viper.service.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viper.config.ai.AiProperties;
import com.viper.pojo.AiChatMessage;
import com.viper.security.UserContext;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.output.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 对话核心服务（流式 + Tool Calling 循环）。
 * <p>
 * 工作流程：
 *   1. 拼装上下文：system prompt + 历史消息（最近 N 条）+ 当前用户问句；
 *   2. 把 ToolSpecification 列表传给 StreamingChatLanguageModel.generate()；
 *   3. onNext：模型吐 token，立即 SSE 推给前端；
 *   4. onComplete：
 *      - 若 response.aiMessage().hasToolExecutionRequests()，则在白名单内执行工具，
 *        把结果作为 ToolExecutionResultMessage 加回历史，再次 generate() —— 形成多轮工具调用循环；
 *      - 否则视为最终回答，落库并 SseEmitter.complete()。
 */
@Service
public class AiChatService {

    private static final Logger log = LoggerFactory.getLogger(AiChatService.class);
    private static final ObjectMapper JSON = new ObjectMapper();
    /** 单次问答允许的最大工具调用轮次，防止 LLM 死循环 */
    private static final int MAX_TOOL_ROUNDS = 5;

    private final ObjectProvider<OpenAiStreamingChatModel> modelProvider; // 可能未装配（AI 关闭时）
    private final AiToolRegistry toolRegistry;
    private final ConversationService conversationService;
    private final AiProperties props;

    public AiChatService(ObjectProvider<OpenAiStreamingChatModel> modelProvider,
                         AiToolRegistry toolRegistry,
                         ConversationService conversationService,
                         AiProperties props) {
        this.modelProvider = modelProvider;
        this.toolRegistry = toolRegistry;
        this.conversationService = conversationService;
        this.props = props;
    }

    public boolean enabled() {
        return props.isEnabled() && modelProvider.getIfAvailable() != null;
    }

    /**
     * 流式回答用户问题，把 token 直接推到 SseEmitter。
     * <p>调用方负责：在 user 消息已落库后再调用；UserContext 已设置。</p>
     */
    public void stream(Long sessionId, String userQuestion, SseEmitter emitter) {
        if (!enabled()) {
            sendError(emitter, "AI 助手未启用或未配置 API Key");
            return;
        }
        // 捕获当前线程的登录上下文，方便工具回调里恢复（streaming 回调在异步线程）
        final UserContext.CurrentUser currentUser = UserContext.require();
        List<ChatMessage> history = buildHistory(sessionId, userQuestion);
        runStreamingRound(emitter, currentUser, sessionId, history, new StringBuilder(), 0);
    }

    private void runStreamingRound(SseEmitter emitter,
                                   UserContext.CurrentUser currentUser,
                                   Long sessionId,
                                   List<ChatMessage> messages,
                                   StringBuilder assistantBuffer,
                                   int round) {
        if (round >= MAX_TOOL_ROUNDS) {
            sendError(emitter, "工具调用轮次超过上限（" + MAX_TOOL_ROUNDS + "），中止本次回答");
            return;
        }
        OpenAiStreamingChatModel model = modelProvider.getObject();

        model.generate(messages, toolRegistry.specifications(), new StreamingResponseHandler<>() {

            @Override public void onNext(String token) {
                assistantBuffer.append(token);
                sendEvent(emitter, "delta", token);
            }

            @Override public void onComplete(Response<AiMessage> response) {
                AiMessage ai = response.content();
                if (ai.hasToolExecutionRequests()) {
                    messages.add(ai);
                    for (ToolExecutionRequest req : ai.toolExecutionRequests()) {
                        messages.add(executeTool(currentUser, sessionId, req, emitter));
                    }
                    runStreamingRound(emitter, currentUser, sessionId, messages, assistantBuffer, round + 1);
                } else {
                    String full = ai.text() != null ? ai.text() : assistantBuffer.toString();
                    try {
                        conversationService.saveMessage(sessionId, "assistant", full);
                        sendEvent(emitter, "done", "");
                        emitter.complete();
                    } catch (Exception ex) {
                        log.error("保存 AI 回复失败", ex);
                        emitter.completeWithError(ex);
                    }
                }
            }

            @Override public void onError(Throwable error) {
                log.error("AI 流式调用失败", error);
                sendError(emitter, "AI 调用失败: " + error.getMessage());
            }
        });
    }

    private ToolExecutionResultMessage executeTool(UserContext.CurrentUser currentUser,
                                                    Long sessionId,
                                                    ToolExecutionRequest req,
                                                    SseEmitter emitter) {
        String name = req.name();
        String args = req.arguments();
        sendEvent(emitter, "tool_call", toolEventJson(name, "arguments", parseJson(args)));

        try {
            // 回调发生在 LangChain4j 的异步线程，需要恢复登录上下文
            UserContext.set(currentUser);
            var executor = toolRegistry.executor(name);
            if (executor == null) {
                String denied = "{\"error\":\"工具未在白名单内: " + name + "\"}";
                conversationService.saveMessageWithTool(sessionId, "tool", denied, name, args, denied);
                sendEvent(emitter, "tool_result", toolEventJson(name, "result", parseJson(denied)));
                return ToolExecutionResultMessage.from(req, denied);
            }
            String result = executor.execute(req, "ai-chat-" + sessionId);
            conversationService.saveMessageWithTool(sessionId, "tool", result, name, args, result);
            sendEvent(emitter, "tool_result", toolEventJson(name, "result", parseJson(result)));
            return ToolExecutionResultMessage.from(req, result);
        } catch (Exception ex) {
            log.error("工具执行失败 name={}", name, ex);
            String err = "{\"error\":\"" + ex.getMessage() + "\"}";
            conversationService.saveMessageWithTool(sessionId, "tool", err, name, args, err);
            sendEvent(emitter, "tool_result", toolEventJson(name, "result", parseJson(err)));
            return ToolExecutionResultMessage.from(req, err);
        } finally {
            UserContext.clear();
        }
    }

    /** 把数据库中保存的消息序列还原为 LangChain4j 的 ChatMessage 列表，并加上系统提示词与本次用户问题 */
    private List<ChatMessage> buildHistory(Long sessionId, String userQuestion) {
        List<ChatMessage> result = new ArrayList<>();
        result.add(SystemMessage.from(props.getSystemPrompt() == null ? "" : props.getSystemPrompt()));

        List<AiChatMessage> all = conversationService.listMessages(sessionId);
        int window = props.getMemoryWindow() == null ? 20 : props.getMemoryWindow();
        int start = Math.max(0, all.size() - window);
        for (int i = start; i < all.size(); i++) {
            AiChatMessage m = all.get(i);
            switch (m.getRole()) {
                case "user"      -> result.add(UserMessage.from(m.getContent()));
                case "assistant" -> result.add(AiMessage.from(m.getContent()));
                default          -> { /* skip system / tool —— 不回灌避免协议复杂度 */ }
            }
        }
        result.add(UserMessage.from(userQuestion));
        return result;
    }

    // ---------------- SSE & JSON helpers ----------------

    private void sendEvent(SseEmitter emitter, String name, String data) {
        try {
            emitter.send(SseEmitter.event().name(name).data(data));
        } catch (IOException e) {
            log.debug("SSE 写入失败（客户端可能已断开）: {}", e.getMessage());
        }
    }

    private void sendError(SseEmitter emitter, String msg) {
        try {
            emitter.send(SseEmitter.event().name("error").data(msg));
            emitter.complete();
        } catch (IOException ignored) { }
    }

    /**
     * 构造 {"name": <name>, <payloadKey>: <payload>} 的 JSON。
     * payload 可以是 Map / List / String / Number 等任何 Jackson 可序列化对象，
     * 前端拿到的就是一个干净的对象（不会被双层 escape）。
     */
    private static String toolEventJson(String name, String payloadKey, Object payload) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", name);
        m.put(payloadKey, payload);
        try { return JSON.writeValueAsString(m); }
        catch (JsonProcessingException e) { return "{\"name\":\"" + name + "\"}"; }
    }

    /** 尽力解析为 JsonNode；解析失败回退为字符串 raw，避免前端拿到双层 escape */
    private static Object parseJson(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            JsonNode node = JSON.readTree(raw);
            return node;
        } catch (Exception e) {
            return raw;
        }
    }
}
