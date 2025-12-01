package com.viper.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("`smbms_provider`")
public class Provider {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;   //id
    private String proCode; //供应商编码
    private String proName; //供应商名称
    private String proDesc; //供应商描述
    private String proContact; //供应商联系人
    private String proPhone; //供应商电话
    private String userAddress; //供应商地址
    private String userFax; //供应商传真
    private Long createdBy; //创建者
    private Date creationDate; //创建时间
    private Long modifyBy; //更新者
    private Date modifyDate;//更新时间
}