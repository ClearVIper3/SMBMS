package com.viper.service.role;

import com.viper.dao.role.RoleMapper;
import com.viper.pojo.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;

@Service("roleService")
@Transactional
public class RoleServiceImpl implements RoleService {

    private final RoleMapper roleMapper;

    @Autowired
    public RoleServiceImpl(RoleMapper roleMapper) {
        this.roleMapper = roleMapper;
    }

    @Override
    public List<Role> getRoleList() {
        List<Role> roles = null;

        try{
            roles = roleMapper.getRoleList();
        } catch(SQLException e){
            e.printStackTrace();
        }
        return roles;
    }
}
