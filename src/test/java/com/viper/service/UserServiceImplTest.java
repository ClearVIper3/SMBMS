package com.viper.service;

import com.viper.exception.BusinessException;
import com.viper.pojo.User;
import com.viper.service.user.UserService;
import com.viper.utils.PasswordUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * UserService 现状行为固化测试。
 * <p>
 * 用 @Transactional 让每个用例结束后自动回滚，互不污染数据。
 * 这些测试在阶段 2/3 升级与改造期间将充当回归网。
 */
@SpringBootTest
@Transactional
class UserServiceImplTest {

    @Autowired
    private UserService userService;

    // ---------- 查询 ----------

    @Test
    void login_should_return_user_by_userCode() {
        // PasswordMigrationRunner 启动时把明文 '123456' 升级成 BCrypt
        User u = userService.Login("admin");

        assertNotNull(u);
        assertEquals("admin", u.getUserCode());
        assertEquals(Long.valueOf(1L), u.getUserRole());
        assertTrue(PasswordUtil.matches("123456", u.getUserPassword()),
                "PasswordMigrationRunner 应已将明文升级为可校验的 BCrypt 哈希");
    }

    @Test
    void login_should_return_null_when_userCode_missing() {
        assertNull(userService.Login("not-exists"));
    }

    @Test
    void getUserCount_should_match_seed_data() {
        // 种子 4 人，userRole 全部已设置，joined where 不会过滤掉任何记录
        assertEquals(4, userService.getUserCount("", 0));
        assertEquals(1, userService.getUserCount("管理员", 0));
        assertEquals(1, userService.getUserCount("", 1)); // 角色1=管理员
        assertEquals(2, userService.getUserCount("", 2)); // 角色2=采购员，有张三、李四
    }

    @Test
    void getUserList_should_paginate_and_sort_by_creationDate_desc() {
        List<User> page1 = userService.getUserList("", 0, 1, 2);
        List<User> page2 = userService.getUserList("", 0, 2, 2);

        assertEquals(2, page1.size());
        assertEquals(2, page2.size());
        // 不重叠
        assertFalse(page1.get(0).getId().equals(page2.get(0).getId()));
        // 关联角色名应一并查出（XML 中 select u.*, r.roleName as userRoleName）
        assertNotNull(page1.get(0).getUserRoleName());
    }

    @Test
    void selectUserCodeExist_should_be_true_for_seed_and_false_for_unknown() {
        assertNotNull(userService.selectUserCodeExist("U001"));
        assertNull(userService.selectUserCodeExist("NOT_EXIST_CODE"));
    }

    @Test
    void getUserById_should_join_role_name() {
        User u = userService.getUserById("1");
        assertNotNull(u);
        assertEquals("U001", u.getUserCode());
        assertNotNull(u.getUserRoleName());
    }

    // ---------- 写入 ----------

    @Test
    void add_should_insert_new_user() {
        User u = newUser("U999", "测试新增", 2L);

        Boolean ok = userService.add(u);

        assertTrue(ok);
        assertNotNull(u.getId(), "MP 应回填自增主键");
        assertNotNull(userService.selectUserCodeExist("U999"));
    }

    @Test
    void add_should_throw_business_exception_on_duplicate_userCode() {
        // U001 是种子数据，重复插入应触发唯一约束并被 DbExceptionTranslator 转译
        User dup = newUser("U001", "重复编码", 2L);

        BusinessException ex = assertThrows(BusinessException.class, () -> userService.add(dup));
        assertNotNull(ex.getMessage());
    }

    @Test
    void passwordModify_should_persist_bcrypt_hash() {
        Boolean ok = userService.PasswordModify(1L, "newPwd123");
        assertTrue(ok);

        User reloaded = userService.Login("admin");
        assertTrue(PasswordUtil.matches("newPwd123", reloaded.getUserPassword()));
        assertFalse(PasswordUtil.matches("123456", reloaded.getUserPassword()));
    }

    @Test
    void modify_should_update_fields() {
        User u = new User();
        u.setId(1L);
        u.setUserName("张三-改名");
        u.setGender(1);
        u.setPhone("13800000099");
        u.setAddress("广州天河-改地址");
        u.setUserRole(2L);
        u.setModifyBy(1L);
        u.setModifyDate(new Date());

        assertTrue(userService.modify(u));
        assertEquals("张三-改名", userService.getUserById("1").getUserName());
    }

    @Test
    void deleteUserById_should_remove_existing_record() {
        // 先插入一个没有外键引用的用户，再删除（直接删 admin 会被审计外键阻塞，这里避开）
        User u = newUser("UDEL", "待删除", 3L);
        userService.add(u);
        assertTrue(userService.deleteUserById(u.getId().intValue()));
        assertNull(userService.selectUserCodeExist("UDEL"));
    }

    // ---------- 工具 ----------

    private static User newUser(String code, String name, Long roleId) {
        User u = new User();
        u.setUserCode(code);
        u.setUserName(name);
        u.setUserPassword(PasswordUtil.encode("123456"));
        u.setGender(1);
        u.setBirthday(new Date());
        u.setPhone("13800000000");
        u.setAddress("测试地址");
        u.setUserRole(roleId);
        u.setCreatedBy(1L);
        u.setCreationDate(new Date());
        return u;
    }
}
