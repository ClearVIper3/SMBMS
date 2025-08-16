package com.viper.service.user;

import com.viper.pojo.User;

import java.util.List;

public interface UserService {
    //用户登录
    public User Login(String userCode, String password);

    public Boolean PasswordModify(int id, String password);

    public int getUserCount(String username,int userRole);

    public List<User> getUserList(String queryUserName, int queryUserRole, int currentPageNo, int pageSize);

    //根据用户编码，判断用户是否存在
    public User selectUserCodeExist(String userCode);

    public Boolean add(User user);

    public Boolean deleteUserById(int delid);

    public User getUserById(String id);

    //修改用户信息
    public Boolean modify(User user) throws Exception;
}
