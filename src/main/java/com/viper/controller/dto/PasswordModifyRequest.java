package com.viper.controller.dto;

import lombok.Data;

@Data
public class PasswordModifyRequest {
    private String oldPassword;
    private String newPassword;
}
