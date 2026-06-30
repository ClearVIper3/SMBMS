package com.viper.controller.dto;

import lombok.Data;

@Data
public class AiChatRequest {
    /** 用户问题 */
    private String message;
}
