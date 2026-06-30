package com.viper.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.viper.pojo.Provider;
import lombok.Data;

import java.util.Date;

@Data
public class ProviderDTO {
    private Long id;
    private String proCode;
    private String proName;
    private String proDesc;
    private String proContact;
    private String proPhone;
    private String userAddress;
    private String userFax;
    private Long createdBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date creationDate;
    private Long modifyBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date modifyDate;

    public static ProviderDTO fromEntity(Provider p) {
        if (p == null) return null;
        ProviderDTO d = new ProviderDTO();
        d.id = p.getId();
        d.proCode = p.getProCode();
        d.proName = p.getProName();
        d.proDesc = p.getProDesc();
        d.proContact = p.getProContact();
        d.proPhone = p.getProPhone();
        d.userAddress = p.getUserAddress();
        d.userFax = p.getUserFax();
        d.createdBy = p.getCreatedBy();
        d.creationDate = p.getCreationDate();
        d.modifyBy = p.getModifyBy();
        d.modifyDate = p.getModifyDate();
        return d;
    }
}
