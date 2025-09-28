package com.viper.dao.role;

import com.viper.pojo.Role;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

@Repository("roleDao")
public class RoleDaoImpl implements RoleDao {

    private final SqlSessionTemplate sqlSession;

    @Autowired
    public RoleDaoImpl(SqlSessionTemplate sqlSession) {
        this.sqlSession = sqlSession;
    }

    @Override
    public List<Role> getRoleList() throws SQLException {
        return sqlSession.getMapper(RoleDao.class).getRoleList();
    }
}
