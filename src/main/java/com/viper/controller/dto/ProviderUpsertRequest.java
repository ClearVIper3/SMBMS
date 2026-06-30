package com.viper.controller.dto;

import com.viper.pojo.Provider;
import lombok.Data;

import java.util.Date;

@Data
public class ProviderUpsertRequest {
    /** 仅新增使用，修改时忽略 */
    private String proCode;
    private String proName;
    private String proDesc;
    private String proContact;
    private String proPhone;
    private String userAddress;
    private String userFax;

    public Provider toNewEntity(Long operatorId) {
        Provider p = baseEntity();
        p.setProCode(proCode);
        p.setCreatedBy(operatorId);
        p.setCreationDate(new Date());
        return p;
    }

    public Provider toUpdateEntity(Long id, Long operatorId) {
        Provider p = baseEntity();
        p.setId(id);
        p.setModifyBy(operatorId);
        p.setModifyDate(new Date());
        return p;
    }

    private Provider baseEntity() {
        Provider p = new Provider();
        p.setProName(proName);
        p.setProDesc(proDesc);
        p.setProContact(proContact);
        p.setProPhone(proPhone);
        p.setUserAddress(userAddress);
        p.setUserFax(userFax);
        return p;
    }
}
