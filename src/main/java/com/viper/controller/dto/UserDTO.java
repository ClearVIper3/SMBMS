package com.viper.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.viper.pojo.User;
import lombok.Data;

import java.util.Date;

/**
 * 用户接口出参/入参 DTO。
 * <p>故意不暴露 {@code userPassword}，避免任何场景下泄露密码哈希。</p>
 */
@Data
public class UserDTO {
    private Long id;
    private String userCode;
    private String userName;
    private Integer gender;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Shanghai")
    private Date birthday;
    private String phone;
    private String address;
    private Long userRole;
    private String userRoleName;
    private Long createdBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date creationDate;
    private Long modifyBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date modifyDate;
    private Integer age;

    public static UserDTO fromEntity(User u) {
        if (u == null) return null;
        UserDTO d = new UserDTO();
        d.id = u.getId();
        d.userCode = u.getUserCode();
        d.userName = u.getUserName();
        d.gender = u.getGender();
        d.birthday = u.getBirthday();
        d.phone = u.getPhone();
        d.address = u.getAddress();
        d.userRole = u.getUserRole();
        d.userRoleName = u.getUserRoleName();
        d.createdBy = u.getCreatedBy();
        d.creationDate = u.getCreationDate();
        d.modifyBy = u.getModifyBy();
        d.modifyDate = u.getModifyDate();
        d.age = u.getAge();
        return d;
    }
}
