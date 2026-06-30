package com.viper.service.role;

import com.viper.dao.role.RoleMapper;
import com.viper.pojo.Role;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleMapper roleMapper;

    public RoleServiceImpl(RoleMapper roleMapper) {
        this.roleMapper = roleMapper;
    }

    @Override
    public List<Role> getRoleList() {
        return roleMapper.selectList(null);
    }
}
