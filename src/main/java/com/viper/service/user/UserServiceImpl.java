package com.viper.service.user;

import com.viper.dao.user.UserDao;
import com.viper.pojo.User;
import java.sql.SQLException;
import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class UserServiceImpl implements UserService {

    private final UserDao userDao;

    public UserServiceImpl() {
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        userDao = context.getBean("userDao",UserDao.class);
    }

    @Override
    public Boolean modify(User user) {

        boolean flag=false;
        try {
            int updateNum = userDao.modify(user);//执行修改sql
            if(updateNum>0){
                flag=true;
                System.out.println("修改用户成功");
            }else{
                System.out.println("修改用户失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            //若抛出异常，则说明修改失败需要回滚
            System.out.println("修改失败，回滚事务");
        }
        return flag;
    }

    @Override
    public User Login(String userCode, String password) {

        User user = null;
        try{
            user = userDao.getLoginUser(userCode);
        } catch(Exception e){
            e.printStackTrace();
        }
        return user;
    }

    @Override
    public Boolean PasswordModify(int id, String password) {

        //System.out.println("Service" + password);
        boolean flag;
        try {
            flag = userDao.UserPasswordModify(id, password) > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return flag;
    }

    @Override
    public int getUserCount(String username, int userRole) {

        int count = 0;

        try{
            count = userDao.getUserCount(username, userRole);
        } catch (Exception e){
            e.printStackTrace();
        }
        return count;
    }

    @Override
    public List<User> getUserList(String queryUserName, int queryUserRole, int currentPageNo, int pageSize) {

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
        }
        return userList;
    }

    public User selectUserCodeExist(String userCode) {

        User user = null;

        try {
            user = userDao.getLoginUser(userCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

    @Override
    public Boolean add(User user) {

        boolean flag = false;

        try {
            int updateRows = userDao.add(user);
            if(updateRows > 0){
                flag = true;
                System.out.println("add success!");
            }else{
                System.out.println("add failed!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("rollback==================");
        }
        return flag;
    }

    @Override
    public User getUserById(String id) {
        User user = new User();

        try {
            user = userDao.getUserById(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  user;
    }

    @Override
    public Boolean deleteUserById(int delId) {
        boolean flag=false;

        try {
            int deleteNum=userDao.deleteUserById(delId);
            if(deleteNum>0)
                flag=true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }
}
