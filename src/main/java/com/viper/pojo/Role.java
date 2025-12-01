package com.viper.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("`smbms_role`")
public class Role {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;             //id
    private String roleCode;        //角色编码
    private String roleName;        //角色名称
    private Long createdBy;      //创建者
    private Date creationDate;      //创建时间
    private Long modifyBy;       //更新者
    private Date modifyDate;        //更新时间
}