package com.viper.service.user;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.viper.dao.user.UserMapper;
import com.viper.exception.BusinessException;
import com.viper.exception.DbExceptionTranslator;
import com.viper.pojo.User;
import java.util.List;

import org.springframework.dao.DataAccessException;
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
        try {
            return userMapper.updateById(user) > 0;
        } catch (DataAccessException e) {
            // 修改时可能违反唯一约束 / 外键约束（如修改为已存在的编码、不存在的角色）
            BusinessException be = DbExceptionTranslator.translate(e);
            throw be != null ? be : new BusinessException("修改用户失败，请稍后重试", e);
        }
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
        try {
            return userMapper.insert(user) > 0;
        } catch (DataAccessException e) {
            // 捕获唯一键冲突（员工编码重复）、外键冲突（角色不存在）等约束异常
            BusinessException be = DbExceptionTranslator.translate(e);
            throw be != null ? be : new BusinessException("添加用户失败，请稍后重试", e);
        }
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
        try {
            return userMapper.deleteById(delId) > 0;
        } catch (DataAccessException e) {
            // 该用户被其它数据以受限外键引用时，删除会失败，返回 false 由上层友好提示
            e.printStackTrace();
            return false;
        }
    }
}
