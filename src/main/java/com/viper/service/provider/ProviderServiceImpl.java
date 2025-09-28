package com.viper.service.provider;

import com.viper.dao.bill.BillDao;
import com.viper.dao.provider.ProviderDao;
import com.viper.pojo.Provider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("providerService")
public class ProviderServiceImpl implements ProviderService {

    private final ProviderDao providerDao;
    private final BillDao billDao;

    @Autowired
    public ProviderServiceImpl(ProviderDao providerDao, BillDao billDao) {
        this.providerDao = providerDao;
        this.billDao = billDao;
    }

    @Override
    public boolean add(Provider provider) {

        boolean flag = false;
        try{
            if(providerDao.add(provider) > 0)
                flag = true;
        } catch(Exception ex) {
            ex.printStackTrace();
            System.out.println("ProviderAddRollback====================");
        }
        return flag;
    }

    @Override
    public int deleteProviderById(String delId) {
        int billCount;

        try{
            billCount = billDao.getBillCountByProviderId(delId);
            if(billCount == 0) {
                providerDao.deleteProviderById(delId);
            }
        } catch(Exception ex) {
            ex.printStackTrace();
            billCount = -1;
        }
        return billCount;
    }

    @Override
    public Provider getProviderById(String id) {

        Provider provider;
        try{
            provider = providerDao.getProviderById(id);
        }catch (Exception e) {
            e.printStackTrace();
            provider = null;
        }
        return provider;
    }

    @Override
    public boolean modify(Provider provider) {
        boolean flag = false;
        try{
            if(providerDao.modify(provider) > 0)
                flag = true;
        } catch (Exception e){
            e.printStackTrace();
        }
        return flag;
    }

    @Override
    public List<Provider> getProviderList(String proName, String proCode) {

        List<Provider> providerList = null;
        System.out.println("query proName ---- > " + proName);
        System.out.println("query proCode ---- > " + proCode);
        try {
            providerList = providerDao.getProviderList(proName,proCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return providerList;
    }
}
