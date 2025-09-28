package com.viper.service.role;

import com.viper.dao.role.RoleDao;
import com.viper.pojo.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

@Service("roleService")
public class RoleServiceImpl implements RoleService {

    private final RoleDao roleDao;

    @Autowired
    public RoleServiceImpl(RoleDao roleDao) {
        this.roleDao = roleDao;
    }

    @Override
    public List<Role> getRoleList() {
        List<Role> roles = null;

        try{
            roles = roleDao.getRoleList();
        } catch(SQLException e){
            e.printStackTrace();
        }
        return roles;
    }
}
