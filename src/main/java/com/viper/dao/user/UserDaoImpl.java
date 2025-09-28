package com.viper.dao.user;

import com.viper.pojo.User;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

@Repository("userDao")
public class UserDaoImpl implements UserDao{

    private SqlSessionTemplate sqlSession;

    @Autowired
    public void setSqlSession(SqlSessionTemplate sqlSession) {
        this.sqlSession = sqlSession;
    }


    @Override
    public User getLoginUser(String userCode) throws SQLException {
        return sqlSession.getMapper(UserDao.class).getLoginUser(userCode);
    }

    @Override
    public int UserPasswordModify(int id, String password) throws SQLException {
        return sqlSession.getMapper(UserDao.class).UserPasswordModify(id, password);
    }

    @Override
    public int getUserCount(String username, int userRole) throws SQLException {
        return sqlSession.getMapper(UserDao.class).getUserCount(username, userRole);
    }

    @Override
    public List<User> getUserList(String userName, int userRole, int startIndex, int pageSize) throws Exception {
        return sqlSession.getMapper(UserDao.class).getUserList(userName, userRole, startIndex, pageSize);
    }

    @Override
    public int add(User user) throws Exception {
        return sqlSession.getMapper(UserDao.class).add(user);
    }

    @Override
    public int deleteUserById(int userid) throws Exception {
        return sqlSession.getMapper(UserDao.class).deleteUserById(userid);
    }

    @Override
    public User getUserById(String id) throws Exception {
        return sqlSession.getMapper(UserDao.class).getUserById(id);
    }

    @Override
    public int modify(User user) throws Exception {
        return sqlSession.getMapper(UserDao.class).modify(user);
    }
}
