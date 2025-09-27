package com.viper.dao.role;

import com.viper.pojo.Role;
import org.mybatis.spring.SqlSessionTemplate;

import java.sql.SQLException;
import java.util.List;

public class RoleDaoImpl implements RoleDao {

    private SqlSessionTemplate sqlSession;

    public void setSqlSession(SqlSessionTemplate sqlSession) {
        this.sqlSession = sqlSession;
    }

    @Override
    public List<Role> getRoleList() throws SQLException {
        return sqlSession.getMapper(RoleDao.class).getRoleList();
    }
}
