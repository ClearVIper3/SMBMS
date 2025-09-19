package com.viper.dao.role;

import com.viper.pojo.Role;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface RoleDao {
    public List<Role> getRoleList() throws SQLException;
}
