package com.viper.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.viper.pojo.AiChatSession;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AiSessionDTO {
    private Long id;
    private String title;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime updateTime;

    public static AiSessionDTO from(AiChatSession s) {
        if (s == null) return null;
        AiSessionDTO d = new AiSessionDTO();
        d.id = s.getId();
        d.title = s.getTitle();
        d.createTime = s.getCreateTime();
        d.updateTime = s.getUpdateTime();
        return d;
    }
}
