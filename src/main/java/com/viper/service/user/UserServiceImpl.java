package com.viper.service.user;

import com.viper.dao.user.UserDao;
import com.viper.pojo.User;
import java.sql.SQLException;
import java.util.List;

import com.viper.utils.MybatisUtils;
import org.apache.ibatis.session.SqlSession;

public class UserServiceImpl implements UserService {
    public UserServiceImpl() {
    }

    @Override
    public Boolean modify(User user) {
        SqlSession sqlSession = MybatisUtils.getSqlSession();
        UserDao userDao = sqlSession.getMapper(UserDao.class);

        Boolean flag=false;
        try {
            int updateNum = userDao.modify(user);//执行修改sql
            if(updateNum>0){
                flag=true;
                System.out.println("修改用户成功");
            }else{
                System.out.println("修改用户失败");
            }
            sqlSession.commit();
        } catch (Exception e) {
            e.printStackTrace();
            //若抛出异常，则说明修改失败需要回滚
            System.out.println("修改失败，回滚事务");
            sqlSession.rollback();
        }finally {
            sqlSession.close();
        }
        return flag;
    }

    @Override
    public User Login(String userCode, String password) {

        //引入Dao层
        SqlSession sqlSession = MybatisUtils.getSqlSession();
        UserDao userDao = sqlSession.getMapper(UserDao.class);
        User user = null;

        try{
            user = userDao.getLoginUser(userCode);
        } catch(Exception e){
            e.printStackTrace();
        }finally {
            sqlSession.close();
        }
        return user;
    }

    @Override
    public Boolean PasswordModify(int id, String password) {

        SqlSession sqlSession = MybatisUtils.getSqlSession();
        UserDao userDao = sqlSession.getMapper(UserDao.class);

        //System.out.println("Service" + password);
        Boolean flag = false;
        try {
            if(userDao.UserPasswordModify(id,password) > 0){
                sqlSession.commit();
                flag = true;
            }else {
                flag = false;
            }
        } catch (SQLException e) {
            sqlSession.rollback();
            throw new RuntimeException(e);
        } finally {
            sqlSession.close();
        }
        return flag;
    }

    @Override
    public int getUserCount(String username, int userRole) {

        SqlSession sqlSession = MybatisUtils.getSqlSession();
        UserDao userDao = sqlSession.getMapper(UserDao.class);
        int count = 0;

        try{
            count = userDao.getUserCount(username, userRole);
        } catch (Exception e){
            e.printStackTrace();
        } finally{
            sqlSession.close();
        }
        return count;
    }

    @Override
    public List<User> getUserList(String queryUserName, int queryUserRole, int currentPageNo, int pageSize) {

        SqlSession sqlSession = MybatisUtils.getSqlSession();
        UserDao userDao = sqlSession.getMapper(UserDao.class);

        int startIndex = (currentPageNo - 1) * pageSize;
        List<User> userList = null;

        System.out.println("queryUserName ---- > " + queryUserName);
        System.out.println("queryUserRole ---- > " + queryUserRole);
        System.out.println("currentPageNo ---- > " + currentPageNo);
        System.out.println("pageSize ---- > " + pageSize);
        try {
            userList = userDao.getUserList(queryUserName,queryUserRole,startIndex,pageSize);
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            sqlSession.close();
        }
        return userList;
    }

    public User selectUserCodeExist(String userCode) {
        SqlSession sqlSession = MybatisUtils.getSqlSession();
        UserDao userDao = sqlSession.getMapper(UserDao.class);
        User user = null;

        try {
            user = userDao.getLoginUser(userCode);
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            sqlSession.close();
        }
        return user;
    }

    @Override
    public Boolean add(User user) {

        SqlSession sqlSession = MybatisUtils.getSqlSession();
        UserDao userDao = sqlSession.getMapper(UserDao.class);

        boolean flag = false;

        try {
            int updateRows = userDao.add(user);
            if(updateRows > 0){
                flag = true;
                System.out.println("add success!");
            }else{
                System.out.println("add failed!");
            }
            sqlSession.commit();

        } catch (Exception e) {
            e.printStackTrace();
            try {
                System.out.println("rollback==================");
                sqlSession.rollback();//失败就回滚
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }finally{
            //在service层进行连接的关闭
            sqlSession.close();
        }
        return flag;
    }

    @Override
    public User getUserById(String id) {
        SqlSession sqlSession = MybatisUtils.getSqlSession();
        UserDao userDao = sqlSession.getMapper(UserDao.class);
        User user = new User();

        try {
            user = userDao.getUserById(id);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            sqlSession.close();
        }
        return  user;
    }

    @Override
    public Boolean deleteUserById(int delId) {
        SqlSession sqlSession = MybatisUtils.getSqlSession();
        UserDao userDao = sqlSession.getMapper(UserDao.class);
        Boolean flag=false;

        try {
            int deleteNum=userDao.deleteUserById(delId);
            if(deleteNum>0)
                flag=true;
            sqlSession.commit();
        } catch (Exception e) {
            e.printStackTrace();
            sqlSession.rollback();
        }finally {
            sqlSession.close();
        }
        return flag;
    }
}
