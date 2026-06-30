package com.viper.controller.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private Long userId;
    private String userCode;
    private String userName;
    private Long roleId;
}
