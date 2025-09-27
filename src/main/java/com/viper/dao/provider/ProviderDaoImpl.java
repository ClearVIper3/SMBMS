package com.viper.dao.provider;

import com.viper.pojo.Provider;
import org.mybatis.spring.SqlSessionTemplate;

import java.util.List;

public class ProviderDaoImpl implements ProviderDao {

    private SqlSessionTemplate sqlSession;

    public void setSqlSession(SqlSessionTemplate sqlSession) {
        this.sqlSession = sqlSession;
    }

    @Override
    public int add(Provider provider) throws Exception {
        return sqlSession.getMapper(ProviderDao.class).add(provider);
    }

    @Override
    public List<Provider> getProviderList(String proName, String proCode) throws Exception {
        return sqlSession.getMapper(ProviderDao.class).getProviderList(proName, proCode);
    }

    @Override
    public int deleteProviderById(String delId) throws Exception {
        return  sqlSession.getMapper(ProviderDao.class).deleteProviderById(delId);
    }

    @Override
    public Provider getProviderById(String id) throws Exception {
        return  sqlSession.getMapper(ProviderDao.class).getProviderById(id);
    }

    @Override
    public int modify(Provider provider) throws Exception {
        return sqlSession.getMapper(ProviderDao.class).modify(provider);
    }
}
