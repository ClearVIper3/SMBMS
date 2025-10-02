package com.viper.dao.role;

import com.viper.pojo.Role;

import java.sql.SQLException;
import java.util.List;

public interface RoleMapper {
    public List<Role> getRoleList() throws SQLException;
}
