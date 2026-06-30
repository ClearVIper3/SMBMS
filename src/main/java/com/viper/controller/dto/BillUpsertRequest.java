package com.viper.controller.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BillUpsertRequest {
    private String billCode;       // 仅新增使用
    private String productName;
    private String productDesc;
    private String productUnit;
    private BigDecimal productCount;
    private BigDecimal totalPrice;
    private Integer isPayment;
    private Long providerId;
}
