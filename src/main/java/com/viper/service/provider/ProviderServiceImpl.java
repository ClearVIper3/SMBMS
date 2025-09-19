package com.viper.service.provider;

import com.viper.dao.bill.BillDao;
import com.viper.dao.provider.ProviderDao;
import com.viper.pojo.Provider;
import com.viper.utils.MybatisUtils;
import org.apache.ibatis.session.SqlSession;

import java.util.List;

public class ProviderServiceImpl implements ProviderService {
    public ProviderServiceImpl(){
    }

    @Override
    public boolean add(Provider provider) {
        SqlSession sqlSession = MybatisUtils.getSqlSession();
        ProviderDao providerDao = sqlSession.getMapper(ProviderDao.class);

        boolean flag = false;
        try{
            if(providerDao.add(provider) > 0)
                flag = true;
            sqlSession.commit();
        } catch(Exception ex) {
            ex.printStackTrace();
            try{
                System.out.println("ProviderAddRollback====================");
                sqlSession.rollback();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } finally{
            sqlSession.close();
        }
        return flag;
    }

    @Override
    public int deleteProviderById(String delId) {
        int billCount = -1;
        SqlSession sqlSession = MybatisUtils.getSqlSession();
        BillDao billDao = sqlSession.getMapper(BillDao.class);
        ProviderDao providerDao = sqlSession.getMapper(ProviderDao.class);

        try{
            billCount = billDao.getBillCountByProviderId(delId);
            if(billCount == 0) {
                providerDao.deleteProviderById(delId);
            }
            sqlSession.commit();
        } catch(Exception ex) {
            ex.printStackTrace();
            billCount = -1;
            sqlSession.rollback();
        } finally {
            sqlSession.close();
        }
        return billCount;
    }

    @Override
    public Provider getProviderById(String id) {
        SqlSession sqlSession = MybatisUtils.getSqlSession();
        ProviderDao providerDao = sqlSession.getMapper(ProviderDao.class);

        Provider provider = null;
        try{
            provider = providerDao.getProviderById(id);
        }catch (Exception e) {
            e.printStackTrace();
            provider = null;
        }finally{
            sqlSession.close();
        }
        return provider;
    }

    @Override
    public boolean modify(Provider provider) {
        SqlSession sqlSession = MybatisUtils.getSqlSession();
        ProviderDao providerDao = sqlSession.getMapper(ProviderDao.class);

        boolean flag = false;
        try{
            if(providerDao.modify(provider) > 0)
                flag = true;
            sqlSession.commit();
        } catch (Exception e){
            e.printStackTrace();
            sqlSession.rollback();
        } finally{
            sqlSession.close();
        }
        return flag;
    }

    @Override
    public List<Provider> getProviderList(String proName, String proCode) {
        SqlSession sqlSession = MybatisUtils.getSqlSession();
        ProviderDao providerDao = sqlSession.getMapper(ProviderDao.class);

        List<Provider> providerList = null;
        System.out.println("query proName ---- > " + proName);
        System.out.println("query proCode ---- > " + proCode);
        try {
            providerList = providerDao.getProviderList(proName,proCode);
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            sqlSession.close();
        }
        return providerList;
    }
}
