package com.viper.dao.role;

import com.viper.pojo.Role;
import org.apache.ibatis.annotations.Mapper;

import java.sql.SQLException;
import java.util.List;

@Mapper
public interface RoleMapper {
    public List<Role> getRoleList() throws SQLException;
}
