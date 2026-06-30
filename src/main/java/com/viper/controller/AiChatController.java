package com.viper.controller;

import com.viper.controller.dto.AiChatRequest;
import com.viper.controller.dto.AiMessageDTO;
import com.viper.controller.dto.AiSessionDTO;
import com.viper.pojo.AiChatMessage;
import com.viper.pojo.AiChatSession;
import com.viper.security.UserContext;
import com.viper.service.ai.AiChatService;
import com.viper.service.ai.ConversationService;
import com.viper.utils.Result;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * AI 助手 REST 接口。
 *  GET    /api/ai/sessions                  列出我的会话
 *  POST   /api/ai/sessions                  新建会话
 *  DELETE /api/ai/sessions/{id}             删除会话
 *  GET    /api/ai/sessions/{id}/messages    历史消息
 *  POST   /api/ai/sessions/{id}/messages    提问（SSE 流式响应）
 *  POST   /api/ai/sessions/{id}/regenerate  重新生成（基于上一条 user 消息）
 *  GET    /api/ai/status                    AI 是否可用
 */
@RestController
@RequestMapping("/api/ai")
public class AiChatController {

    private static final long SSE_TIMEOUT_MS = 5 * 60 * 1000L;

    private final ConversationService conversationService;
    private final AiChatService aiChatService;

    /** 独立线程池跑流式生成，避免占用 Servlet 容器线程 */
    private final ExecutorService aiExecutor = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "ai-stream");
        t.setDaemon(true);
        return t;
    });

    public AiChatController(ConversationService conversationService, AiChatService aiChatService) {
        this.conversationService = conversationService;
        this.aiChatService = aiChatService;
    }

    @GetMapping("/status")
    public Result status() {
        return Result.success(java.util.Map.of("enabled", aiChatService.enabled()));
    }

    // ---------------- 会话管理 ----------------

    @GetMapping("/sessions")
    public Result listSessions() {
        List<AiSessionDTO> data = conversationService.listMySessions().stream()
                .map(AiSessionDTO::from).collect(Collectors.toList());
        return Result.success(data);
    }

    @PostMapping("/sessions")
    public Result createSession(@RequestBody(required = false) AiSessionDTO body) {
        Long uid = UserContext.require().getId();
        String title = body == null ? null : body.getTitle();
        AiChatSession s = conversationService.createSession(uid, title);
        return Result.success(AiSessionDTO.from(s));
    }

    @DeleteMapping("/sessions/{id}")
    public Result deleteSession(@PathVariable("id") Long id) {
        conversationService.deleteSession(id);
        return Result.success("已删除");
    }

    @GetMapping("/sessions/{id}/messages")
    public Result listMessages(@PathVariable("id") Long id) {
        List<AiMessageDTO> list = conversationService.listMessages(id).stream()
                .map(AiMessageDTO::from).collect(Collectors.toList());
        return Result.success(list);
    }

    // ---------------- 对话（SSE） ----------------

    @PostMapping(value = "/sessions/{id}/messages", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@PathVariable("id") Long sessionId, @RequestBody AiChatRequest req) {
        AiChatSession session = conversationService.requireOwnedSession(sessionId);
        String question = req == null ? null : req.getMessage();
        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("message 不能为空");
        }
        // 1) 用户消息先落库 2) 默认标题自动改为首问 3) 异步流式生成
        conversationService.saveMessage(sessionId, "user", question);
        conversationService.renameIfDefault(sessionId, question);

        return startStream(sessionId, question);
    }

    @PostMapping(value = "/sessions/{id}/regenerate", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter regenerate(@PathVariable("id") Long sessionId) {
        conversationService.requireOwnedSession(sessionId);
        // 删掉最后一条 assistant，再以倒数第二条 user 作为输入重新提问
        conversationService.deleteLastAssistantMessage(sessionId);
        List<AiChatMessage> all = conversationService.listMessages(sessionId);
        AiChatMessage lastUser = null;
        for (int i = all.size() - 1; i >= 0; i--) {
            if ("user".equals(all.get(i).getRole())) { lastUser = all.get(i); break; }
        }
        if (lastUser == null) {
            throw new IllegalArgumentException("当前会话没有可重新生成的用户消息");
        }
        // 把 user 从 buildHistory 里再次带进去（不再二次落库）
        return startStreamWithoutPersistUser(sessionId, lastUser.getContent());
    }

    // ---------------- helpers ----------------

    private SseEmitter startStream(Long sessionId, String question) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        UserContext.CurrentUser cu = UserContext.require();
        aiExecutor.submit(() -> {
            try {
                UserContext.set(cu);
                aiChatService.stream(sessionId, question, emitter);
            } finally {
                UserContext.clear();
            }
        });
        emitter.onTimeout(emitter::complete);
        emitter.onError(t -> emitter.complete());
        return emitter;
    }

    private SseEmitter startStreamWithoutPersistUser(Long sessionId, String question) {
        return startStream(sessionId, question);
    }
}
