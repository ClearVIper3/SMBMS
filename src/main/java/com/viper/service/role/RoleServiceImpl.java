package com.viper.service.role;

import com.viper.dao.role.RoleDao;
import com.viper.pojo.Role;
import com.viper.utils.MybatisUtils;
import org.apache.ibatis.session.SqlSession;

import java.sql.SQLException;
import java.util.List;

public class RoleServiceImpl implements RoleService {

    public RoleServiceImpl() {
    }

    @Override
    public List<Role> getRoleList() {
        SqlSession sqlSession = MybatisUtils.getSqlSession();
        RoleDao roleDao =  sqlSession.getMapper(RoleDao.class);
        List<Role> roles = null;

        try{
            roles = roleDao.getRoleList();
        } catch(SQLException e){
            e.printStackTrace();
        } finally{
            sqlSession.close();
        }
        return roles;
    }
}
