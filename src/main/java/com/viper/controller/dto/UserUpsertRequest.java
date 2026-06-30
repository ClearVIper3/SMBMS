package com.viper.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/** 新增/修改用户的统一入参。新增时 userPassword 必填，修改时可空（不修改密码）。 */
@Data
public class UserUpsertRequest {
    private String userCode;
    private String userName;
    private String userPassword;
    private Integer gender;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date birthday;
    private String phone;
    private String address;
    private Long userRole;
}
