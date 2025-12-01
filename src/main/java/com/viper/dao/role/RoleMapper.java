package com.viper.dao.role;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.viper.pojo.Role;
import org.apache.ibatis.annotations.Mapper;

import java.sql.SQLException;
import java.util.List;

@Mapper
public interface RoleMapper extends BaseMapper<Role> {
}
