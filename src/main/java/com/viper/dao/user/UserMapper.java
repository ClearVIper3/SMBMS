package com.viper.dao.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.viper.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.sql.SQLException;
import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    //根据用户输入的名字或者角色id来查询计算用户数量
    int getUserCount(@Param("userName") String username,@Param("userRole") int userRole) throws SQLException;

    //通过用户输入的条件查询用户列表
    List<User> getUserList(@Param("userName") String userName,@Param("userRole") int userRole,@Param("startIndex") int startIndex,@Param("pageSize") int pageSize) throws  Exception;

    //通过userId查看当前用户信息
    User getUserById(@Param("id") String id)throws Exception;
}
