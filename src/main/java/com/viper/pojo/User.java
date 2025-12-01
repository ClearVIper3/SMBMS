package com.viper.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("`smbms_user`")
public class User {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;     //id
    private String userCode;    //用户编码
    private String userName;     //用户名称
    private String userPassword;    //用户密码
    private Integer gender;        //性别
    private Date birthday;      //出生日期
    private String phone;       //电话
    private String address;     //地址
    private Long userRole;   //用户角色
    private Long createdBy;  //创建者
    private Date creationDate;  //创建时间
    private Long modifyBy;   //更新者
    private Date modifyDate;    //更新时间

    @TableField(exist = false)
    private Integer age;    //年龄
    @TableField(exist = false)
    private String userRoleName;    //用户角色名称
}