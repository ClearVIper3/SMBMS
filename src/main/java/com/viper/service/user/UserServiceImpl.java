package com.viper.service.user;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.viper.dao.user.UserMapper;
import com.viper.exception.DbExceptionTranslator;
import com.viper.pojo.User;
import com.viper.utils.PasswordUtil;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public User Login(String userCode) {
        return userMapper.selectOne(new QueryWrapper<User>().eq("userCode", userCode));
    }

    @Override
    public Boolean PasswordModify(Long id, String password) {
        UpdateWrapper<User> w = new UpdateWrapper<>();
        w.eq("id", id).set("userPassword", PasswordUtil.encode(password));
        return userMapper.update(w) > 0;
    }

    @Override
    public int getUserCount(String username, Integer userRole) {
        return userMapper.getUserCount(username, userRole);
    }

    @Override
    public List<User> getUserList(String queryUserName, Integer queryUserRole,
                                  Integer currentPageNo, Integer pageSize) {
        int startIndex = (currentPageNo - 1) * pageSize;
        return userMapper.getUserList(queryUserName, queryUserRole, startIndex, pageSize);
    }

    @Override
    public User selectUserCodeExist(String userCode) {
        return userMapper.selectOne(new QueryWrapper<User>().eq("userCode", userCode));
    }

    @Override
    public Boolean add(User user) {
        return DbExceptionTranslator.execute(
                () -> userMapper.insert(user) > 0,
                "添加用户失败，请稍后重试");
    }

    @Override
    public Boolean modify(User user) {
        return DbExceptionTranslator.execute(
                () -> userMapper.updateById(user) > 0,
                "修改用户失败，请稍后重试");
    }

    @Override
    public User getUserById(String id) {
        return userMapper.getUserById(id);
    }

    @Override
    public Boolean deleteUserById(Integer delId) {
        // 被其它数据以受限外键引用时删除会失败，统一返回 false 由上层友好提示
        try {
            return userMapper.deleteById(delId) > 0;
        } catch (DataAccessException ignored) {
            return false;
        }
    }
}
