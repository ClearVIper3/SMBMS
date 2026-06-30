package com.viper.controller.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String userCode;
    private String userPassword;
}
