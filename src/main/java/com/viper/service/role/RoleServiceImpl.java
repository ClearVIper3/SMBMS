package com.viper.service.role;

import com.viper.dao.role.RoleDao;
import com.viper.pojo.Role;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.sql.SQLException;
import java.util.List;

public class RoleServiceImpl implements RoleService {

    private final RoleDao roleDao;

    public RoleServiceImpl() {
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        roleDao = context.getBean("roleDao",RoleDao.class);
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
