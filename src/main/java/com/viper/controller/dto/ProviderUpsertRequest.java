package com.viper.controller.dto;

import lombok.Data;

@Data
public class ProviderUpsertRequest {
    private String proCode;        // 仅新增使用
    private String proName;
    private String proDesc;
    private String proContact;
    private String proPhone;
    private String userAddress;
    private String userFax;
}
