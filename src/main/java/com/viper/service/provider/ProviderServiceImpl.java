package com.viper.service.provider;

import com.viper.dao.BaseDao;
import com.viper.dao.bill.BillDao;
import com.viper.dao.bill.BillDaoImpl;
import com.viper.dao.provider.ProviderDao;
import com.viper.dao.provider.ProviderDaoImpl;
import com.viper.pojo.Provider;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class ProviderServiceImpl implements ProviderService {

    private ProviderDao providerDao;
    private BillDao billDao;
    public ProviderServiceImpl(){
        providerDao = new ProviderDaoImpl();
        billDao = new BillDaoImpl();
    }

    @Override
    public boolean add(Provider provider) {
        Connection connection = null;
        boolean flag = false;

        try{
            connection = BaseDao.getConnection();
            connection.setAutoCommit(false);
            if(providerDao.add(connection,provider) > 0)
                flag = true;
            connection.commit();
        } catch(Exception ex) {
            ex.printStackTrace();
            try{
                System.out.println("ProviderAddRollback====================");
                connection.rollback();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } finally{
            BaseDao.closeResource(connection,null,null);
        }
        return flag;
    }

    @Override
    public int deleteProviderById(String delId) {
        Connection conn = null;
        int billCount = -1;

        try{
            conn = BaseDao.getConnection();
            conn.setAutoCommit(false);
            billCount = billDao.getBillCountByProviderId(conn, delId);
            if(billCount == 0) {
                providerDao.deleteProviderById(conn, delId);
            }
            conn.commit();
        } catch(Exception ex) {
            ex.printStackTrace();
            billCount = -1;
            try{
                conn.rollback();
            } catch(SQLException se){
                se.printStackTrace();
            }
        } finally {
            BaseDao.closeResource(conn,null,null);
        }
        return billCount;
    }

    @Override
    public Provider getProviderById(String id) {
        // TODO Auto-generated method stub
        Provider provider = null;
        Connection connection = null;
        try{
            connection = BaseDao.getConnection();
            provider = providerDao.getProviderById(connection, id);
        }catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            provider = null;
        }finally{
            BaseDao.closeResource(connection, null, null);
        }
        return provider;
    }

    @Override
    public boolean modify(Provider provider) {
        Connection connection = null;
        boolean flag = false;
        try{
            connection = BaseDao.getConnection();
            if(providerDao.modify(connection,provider) > 0)
                flag = true;
        } catch (Exception e){
            e.printStackTrace();
        } finally{
            BaseDao.closeResource(connection, null, null);
        }
        return flag;
    }

    @Override
    public List<Provider> getProviderList(String proName, String proCode) {
        Connection connection = null;
        List<Provider> providerList = null;
        System.out.println("query proName ---- > " + proName);
        System.out.println("query proCode ---- > " + proCode);
        try {
            connection = BaseDao.getConnection();
            providerList = providerDao.getProviderList(connection, proName,proCode);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally{
            BaseDao.closeResource(connection, null, null);
        }
        return providerList;
    }
}
