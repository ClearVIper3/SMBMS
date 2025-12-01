package com.viper.service.user;

import com.viper.pojo.User;

import java.util.List;

public interface UserService {
    //用户登录
    User Login(String userCode);

    Boolean PasswordModify(Long id, String password);

    int getUserCount(String username,Integer userRole);

    List<User> getUserList(String queryUserName, Integer queryUserRole, Integer currentPageNo, Integer pageSize);

    //根据用户编码，判断用户是否存在
    User selectUserCodeExist(String userCode);

    Boolean add(User user);

    Boolean deleteUserById(Integer delid);

    User getUserById(String id);

    //修改用户信息
    Boolean modify(User user);
}
