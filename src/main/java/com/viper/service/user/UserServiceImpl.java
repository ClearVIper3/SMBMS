package com.viper.service.user;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.viper.dao.user.UserMapper;
import com.viper.pojo.User;
import java.sql.SQLException;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("userService")
@Transactional
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public Boolean modify(User user) {
        return userMapper.updateById(user) > 0;
    }

    @Override
    public User Login(String userCode, String password) {

        User user = null;
        try{
            user = userMapper.getLoginUser(userCode);
        } catch(Exception e){
            e.printStackTrace();
        }
        return user;
    }

    @Override
    public Boolean PasswordModify(int id, String password) {

        UpdateWrapper<User> updateWrapper = new UpdateWrapper<User>();

        updateWrapper.eq("id",id);
        updateWrapper.set("userPassword",password);

        return userMapper.update(updateWrapper) > 0;
    }

    @Override
    public int getUserCount(String username, int userRole) {

        int count = 0;

        try{
            count = userMapper.getUserCount(username, userRole);
        } catch (Exception e){
            e.printStackTrace();
        }
        return count;
    }

    @Override
    public List<User> getUserList(String queryUserName, int queryUserRole, int currentPageNo, int pageSize) {

        int startIndex = (currentPageNo - 1) * pageSize;
        List<User> userList = null;

        try {
            userList = userMapper.getUserList(queryUserName,queryUserRole,startIndex,pageSize);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userList;
    }

    public User selectUserCodeExist(String userCode) {

        User user = null;

        try {
            user = userMapper.getLoginUser(userCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

    @Override
    public Boolean add(User user) {
        return userMapper.insert(user) > 0;
    }

    @Override
    public User getUserById(String id) {
        return  userMapper.selectById(id);
    }

    @Override
    public Boolean deleteUserById(int delId) {
        return userMapper.deleteById(delId) > 0;
    }
}
