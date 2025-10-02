package com.viper.dao.user;

import com.viper.pojo.User;
import org.apache.ibatis.annotations.Param;

import java.sql.SQLException;
import java.util.List;

public interface UserMapper {
    //得到登录的用户
    //String userPassword
    public User getLoginUser(@Param("userCode") String userCode) throws SQLException;

    //修改当前用户密码
    //增删改都会影响数据库的变化，所以是返回int类型，说明有几行受到了影响
    public int UserPasswordModify(@Param("id") int id,@Param("userPassword") String password) throws SQLException;

    //根据用户输入的名字或者角色id来查询计算用户数量
    public int getUserCount(@Param("userName") String username,@Param("userRole") int userRole) throws SQLException;

    //通过用户输入的条件查询用户列表
    public List<User> getUserList(@Param("userName") String userName,@Param("userRole") int userRole,@Param("startIndex") int startIndex,@Param("pageSize") int pageSize) throws  Exception;

    //增加用户信息
    public int add(User user) throws Exception;

    //通过用户id删除用户信息
    public int deleteUserById(@Param("userid") int userid) throws Exception;

    //通过userId查看当前用户信息
    public User getUserById(@Param("id") String id)throws Exception;

    //修改用户信息
    public int modify(User user)throws Exception;
}
