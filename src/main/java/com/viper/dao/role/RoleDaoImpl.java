package com.viper.dao.role;

import com.viper.dao.BaseDao;
import com.viper.pojo.Role;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RoleDaoImpl implements RoleDao {
    @Override
    public List<Role> getRoleList(Connection connection) throws SQLException {

        PreparedStatement ps = null;
        ResultSet rs = null;

        ArrayList<Role> roleList = new ArrayList<>();

        if(connection != null) {
            String sql = "select * from smbms_role";
            Object[] params = {};
            rs = BaseDao.execute(connection,sql,params,rs,ps);

            while(rs.next()) {
                Role role = new Role();
                role.setId(rs.getInt("id"));
                role.setRoleCode(rs.getString("roleCode"));
                role.setRoleName(rs.getString("roleName"));
                role.setCreatedBy(rs.getInt("createdBy"));
                role.setCreationDate(rs.getDate("creationDate"));
                role.setModifyBy(rs.getInt("modifyBy"));
                role.setModifyDate(rs.getDate("modifyDate"));
                roleList.add(role);
            }
            BaseDao.closeResource(null, ps, rs);
        }
        return roleList;
    }
}
