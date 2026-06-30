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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/** AI 助手 REST 接口（会话管理 + SSE 流式对话）。 */
@RestController
@RequestMapping("/api/ai")
public class AiChatController {

    private static final long SSE_TIMEOUT_MS = 5 * 60 * 1000L;

    private final ConversationService conversationService;
    private final AiChatService aiChatService;

    /**
     * 流式生成专用线程池：有界、可拒绝、daemon。
     * 避免 newCachedThreadPool 无界创建线程在高并发下打挂 JVM。
     */
    private final ExecutorService aiExecutor;

    public AiChatController(ConversationService conversationService, AiChatService aiChatService) {
        this.conversationService = conversationService;
        this.aiChatService = aiChatService;

        AtomicInteger seq = new AtomicInteger();
        this.aiExecutor = new ThreadPoolExecutor(
                4, 32, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(64),
                r -> {
                    Thread t = new Thread(r, "ai-stream-" + seq.incrementAndGet());
                    t.setDaemon(true);
                    return t;
                },
                new ThreadPoolExecutor.AbortPolicy());
    }

    @GetMapping("/status")
    public Result<Map<String, Boolean>> status() {
        return Result.success(Map.of("enabled", aiChatService.enabled()));
    }

    // ---------------- 会话管理 ----------------

    @GetMapping("/sessions")
    public Result<List<AiSessionDTO>> listSessions() {
        return Result.success(conversationService.listMySessions().stream().map(AiSessionDTO::from).toList());
    }

    @PostMapping("/sessions")
    public Result<AiSessionDTO> createSession(@RequestBody(required = false) AiSessionDTO body) {
        Long uid = UserContext.require().getId();
        AiChatSession s = conversationService.createSession(uid, body == null ? null : body.getTitle());
        return Result.success(AiSessionDTO.from(s));
    }

    @DeleteMapping("/sessions/{id}")
    public Result<String> deleteSession(@PathVariable("id") Long id) {
        conversationService.deleteSession(id);
        return Result.successMsg("已删除");
    }

    @GetMapping("/sessions/{id}/messages")
    public Result<List<AiMessageDTO>> listMessages(@PathVariable("id") Long id) {
        return Result.success(conversationService.listMessages(id).stream().map(AiMessageDTO::from).toList());
    }

    // ---------------- 对话（SSE） ----------------

    @PostMapping(value = "/sessions/{id}/messages", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@PathVariable("id") Long sessionId, @RequestBody AiChatRequest req) {
        conversationService.requireOwnedSession(sessionId);
        String question = req == null ? null : req.getMessage();
        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("message 不能为空");
        }
        conversationService.saveMessage(sessionId, "user", question);
        conversationService.renameIfDefault(sessionId, question);
        return startStream(sessionId, question);
    }

    @PostMapping(value = "/sessions/{id}/regenerate", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter regenerate(@PathVariable("id") Long sessionId) {
        conversationService.requireOwnedSession(sessionId);
        conversationService.deleteLastAssistantMessage(sessionId);

        List<AiChatMessage> all = conversationService.listMessages(sessionId);
        AiChatMessage lastUser = null;
        for (int i = all.size() - 1; i >= 0; i--) {
            if ("user".equals(all.get(i).getRole())) { lastUser = all.get(i); break; }
        }
        if (lastUser == null) throw new IllegalArgumentException("当前会话没有可重新生成的用户消息");
        return startStream(sessionId, lastUser.getContent());
    }

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
}
