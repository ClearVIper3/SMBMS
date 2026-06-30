package com.viper.service;

import com.viper.pojo.Role;
import com.viper.service.role.RoleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class RoleServiceImplTest {

    @Autowired
    private RoleService roleService;

    @Test
    void getRoleList_should_return_seed_three_roles() {
        List<Role> list = roleService.getRoleList();
        assertEquals(3, list.size());
        assertEquals("R001", list.get(0).getRoleCode());
    }
}
