package com.viper.service.ai;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.viper.dao.ai.AiChatMessageMapper;
import com.viper.dao.ai.AiChatSessionMapper;
import com.viper.pojo.AiChatMessage;
import com.viper.pojo.AiChatSession;
import com.viper.security.UserContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 会话与消息的持久化操作。
 * 所有方法严格按 user_id 隔离：用户只能读写自己的会话。
 */
@Service
@Transactional
public class ConversationService {

    private final AiChatSessionMapper sessionMapper;
    private final AiChatMessageMapper messageMapper;

    public ConversationService(AiChatSessionMapper sessionMapper, AiChatMessageMapper messageMapper) {
        this.sessionMapper = sessionMapper;
        this.messageMapper = messageMapper;
    }

    public AiChatSession createSession(Long userId, String title) {
        AiChatSession s = new AiChatSession();
        s.setUserId(userId);
        s.setTitle(title == null || title.isBlank() ? "新会话" : title.substring(0, Math.min(40, title.length())));
        s.setCreateTime(LocalDateTime.now());
        s.setUpdateTime(LocalDateTime.now());
        sessionMapper.insert(s);
        return s;
    }

    public List<AiChatSession> listMySessions() {
        Long uid = UserContext.require().getId();
        QueryWrapper<AiChatSession> q = new QueryWrapper<>();
        q.eq("user_id", uid).orderByDesc("update_time").last("limit 100");
        return sessionMapper.selectList(q);
    }

    public AiChatSession requireOwnedSession(Long sessionId) {
        Long uid = UserContext.require().getId();
        AiChatSession s = sessionMapper.selectById(sessionId);
        if (s == null || !s.getUserId().equals(uid)) {
            throw new IllegalArgumentException("会话不存在或无权访问");
        }
        return s;
    }

    public void deleteSession(Long sessionId) {
        requireOwnedSession(sessionId);
        QueryWrapper<AiChatMessage> mq = new QueryWrapper<>();
        mq.eq("session_id", sessionId);
        messageMapper.delete(mq);
        sessionMapper.deleteById(sessionId);
    }

    public List<AiChatMessage> listMessages(Long sessionId) {
        requireOwnedSession(sessionId);
        QueryWrapper<AiChatMessage> q = new QueryWrapper<>();
        q.eq("session_id", sessionId).orderByAsc("id");
        return messageMapper.selectList(q);
    }

    public AiChatMessage saveMessage(Long sessionId, String role, String content) {
        return saveMessageWithTool(sessionId, role, content, null, null, null);
    }

    public AiChatMessage saveMessageWithTool(Long sessionId, String role, String content,
                                             String toolName, String toolArguments, String toolResult) {
        AiChatMessage m = new AiChatMessage();
        m.setSessionId(sessionId);
        m.setRole(role);
        m.setContent(content);
        m.setToolName(toolName);
        m.setToolArguments(toolArguments);
        m.setToolResult(toolResult);
        m.setCreateTime(LocalDateTime.now());
        messageMapper.insert(m);
        touchSession(sessionId);
        return m;
    }

    /** 回滚最后一条 assistant 消息（"重新生成"功能用）*/
    public void deleteLastAssistantMessage(Long sessionId) {
        requireOwnedSession(sessionId);
        QueryWrapper<AiChatMessage> q = new QueryWrapper<>();
        q.eq("session_id", sessionId).orderByDesc("id").last("limit 1");
        AiChatMessage last = messageMapper.selectOne(q);
        if (last != null && ("assistant".equals(last.getRole()) || "tool".equals(last.getRole()))) {
            messageMapper.deleteById(last.getId());
        }
    }

    public void renameIfDefault(Long sessionId, String newTitle) {
        AiChatSession s = sessionMapper.selectById(sessionId);
        if (s == null || newTitle == null || newTitle.isBlank()) return;
        if ("新会话".equals(s.getTitle())) {
            s.setTitle(newTitle.substring(0, Math.min(40, newTitle.length())));
            s.setUpdateTime(LocalDateTime.now());
            sessionMapper.updateById(s);
        }
    }

    private void touchSession(Long sessionId) {
        AiChatSession s = sessionMapper.selectById(sessionId);
        if (s != null) {
            s.setUpdateTime(LocalDateTime.now());
            sessionMapper.updateById(s);
        }
    }
}
