package com.viper.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 会话消息实体。
 * <p>
 * role 取值：
 *   - "user"      用户输入
 *   - "assistant" AI 文本回复
 *   - "system"    系统提示词（一般只在会话开头）
 *   - "tool"      工具执行结果（含 tool_name / tool_arguments / tool_result）
 */
@Data
@TableName("ai_chat_message")
public class AiChatMessage {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("session_id")
    private Long sessionId;

    private String role;

    private String content;

    @TableField("tool_name")
    private String toolName;

    @TableField("tool_arguments")
    private String toolArguments;

    @TableField("tool_result")
    private String toolResult;

    private Integer tokens;

    @TableField(value = "create_time")
    private LocalDateTime createTime;
}
