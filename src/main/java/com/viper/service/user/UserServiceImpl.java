package com.viper.service.user;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.viper.dao.user.UserMapper;
import com.viper.pojo.User;
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
    public User Login(String userCode) {

        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("userCode",userCode);

        return userMapper.selectOne(wrapper);
    }

    @Override
    public Boolean PasswordModify(Long id, String password) {

        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();

        updateWrapper.eq("id",id);
        updateWrapper.set("userPassword",password);

        return userMapper.update(updateWrapper) > 0;
    }

    @Override
    public int getUserCount(String username, Integer userRole) {

        int count = 0;

        try{
            count = userMapper.getUserCount(username, userRole);
        } catch (Exception e){
            e.printStackTrace();
        }
        return count;
    }

    @Override
    public List<User> getUserList(String queryUserName, Integer queryUserRole, Integer currentPageNo, Integer pageSize) {

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

        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("userCode",userCode);

        return userMapper.selectOne(wrapper);
    }

    @Override
    public Boolean add(User user) {
        return userMapper.insert(user) > 0;
    }

    @Override
    public User getUserById(String id) {
        User user = new User();

        try {
            user = userMapper.getUserById(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  user;
    }

    @Override
    public Boolean deleteUserById(Integer delId) {
        return userMapper.deleteById(delId) > 0;
    }
}
