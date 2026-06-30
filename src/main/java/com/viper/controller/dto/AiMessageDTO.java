package com.viper.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.viper.pojo.AiChatMessage;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AiMessageDTO {
    private Long id;
    private String role;
    private String content;
    private String toolName;
    private String toolArguments;
    private String toolResult;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createTime;

    public static AiMessageDTO from(AiChatMessage m) {
        if (m == null) return null;
        AiMessageDTO d = new AiMessageDTO();
        d.id = m.getId();
        d.role = m.getRole();
        d.content = m.getContent();
        d.toolName = m.getToolName();
        d.toolArguments = m.getToolArguments();
        d.toolResult = m.getToolResult();
        d.createTime = m.getCreateTime();
        return d;
    }
}
