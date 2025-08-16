package com.viper.service.role;

import com.viper.dao.BaseDao;
import com.viper.dao.role.RoleDao;
import com.viper.dao.role.RoleDaoImpl;
import com.viper.pojo.Role;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RoleServiceImpl implements RoleService {

    private RoleDao roleDao;

    public RoleServiceImpl() {
        roleDao = new RoleDaoImpl();
    }

    @Override
    public List<Role> getRoleList() {
        Connection connection = null;
        List<Role> roles = null;
        try{
            connection = BaseDao.getConnection();
            roles = roleDao.getRoleList(connection);
        } catch(SQLException e){
            e.printStackTrace();
        } finally{
            BaseDao.closeResource(connection, null, null);
        }
        return roles;
    }
}
