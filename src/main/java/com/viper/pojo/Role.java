package com.viper.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("`smbms_role`")
public class Role {
    private Integer id;             //id
    private String roleCode;        //角色编码
    private String roleName;        //角色名称
    private Integer createdBy;      //创建者
    private Date creationDate;      //创建时间
    private Integer modifyBy;       //更新者
    private Date modifyDate;        //更新时间
}