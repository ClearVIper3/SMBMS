package com.viper.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.viper.pojo.Bill;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class BillDTO {
    private Long id;
    private String billCode;
    private String productName;
    private String productDesc;
    private String productUnit;
    private BigDecimal productCount;
    private BigDecimal totalPrice;
    private Integer isPayment;
    private Long providerId;
    private String providerName;
    private Long createdBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date creationDate;
    private Long modifyBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date modifyDate;

    public static BillDTO fromEntity(Bill b) {
        if (b == null) return null;
        BillDTO d = new BillDTO();
        d.id = b.getId();
        d.billCode = b.getBillCode();
        d.productName = b.getProductName();
        d.productDesc = b.getProductDesc();
        d.productUnit = b.getProductUnit();
        d.productCount = b.getProductCount();
        d.totalPrice = b.getTotalPrice();
        d.isPayment = b.getIsPayment();
        d.providerId = b.getProviderId();
        d.providerName = b.getProviderName();
        d.createdBy = b.getCreatedBy();
        d.creationDate = b.getCreationDate();
        d.modifyBy = b.getModifyBy();
        d.modifyDate = b.getModifyDate();
        return d;
    }
}
