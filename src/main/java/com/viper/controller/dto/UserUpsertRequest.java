package com.viper.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.viper.pojo.User;
import com.viper.utils.PasswordUtil;
import lombok.Data;

import java.util.Date;

/**
 * 新增 / 修改用户的统一入参。
 * 新增时 userCode 与 userPassword 必填；修改时 userCode/userPassword 一律忽略。
 */
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

    /** 用本 DTO 构造一个待 insert 的 User 实体（密码会被 BCrypt 加密）。 */
    public User toNewEntity(Long operatorId) {
        User u = baseEntity();
        u.setUserCode(userCode);
        u.setUserPassword(PasswordUtil.encode(userPassword));
        u.setCreatedBy(operatorId);
        u.setCreationDate(new Date());
        return u;
    }

    /** 用本 DTO 构造一个待 update 的 User 实体（不含 userCode/userPassword）。 */
    public User toUpdateEntity(Long id, Long operatorId) {
        User u = baseEntity();
        u.setId(id);
        u.setModifyBy(operatorId);
        u.setModifyDate(new Date());
        return u;
    }

    private User baseEntity() {
        User u = new User();
        u.setUserName(userName);
        u.setGender(gender);
        u.setBirthday(birthday);
        u.setPhone(phone);
        u.setAddress(address);
        u.setUserRole(userRole);
        return u;
    }
}
