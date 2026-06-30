package com.viper.controller.dto;

import com.viper.pojo.Bill;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class BillUpsertRequest {
    /** 仅新增使用，修改时忽略 */
    private String billCode;
    private String productName;
    private String productDesc;
    private String productUnit;
    private BigDecimal productCount;
    private BigDecimal totalPrice;
    private Integer isPayment;
    private Long providerId;

    public Bill toNewEntity(Long operatorId) {
        Bill b = baseEntity();
        b.setBillCode(billCode);
        b.setCreatedBy(operatorId);
        b.setCreationDate(new Date());
        return b;
    }

    public Bill toUpdateEntity(Long id, Long operatorId) {
        Bill b = baseEntity();
        b.setId(id);
        b.setModifyBy(operatorId);
        b.setModifyDate(new Date());
        return b;
    }

    private Bill baseEntity() {
        Bill b = new Bill();
        b.setProductName(productName);
        b.setProductDesc(productDesc);
        b.setProductUnit(productUnit);
        b.setProductCount(productCount);
        b.setTotalPrice(totalPrice);
        b.setIsPayment(isPayment);
        b.setProviderId(providerId);
        return b;
    }
}
