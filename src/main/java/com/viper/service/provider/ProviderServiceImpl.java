package com.viper.service.provider;

import com.viper.dao.bill.BillMapper;
import com.viper.dao.provider.ProviderMapper;
import com.viper.pojo.Provider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("providerService")
@Transactional
public class ProviderServiceImpl implements ProviderService {

    private final ProviderMapper providerMapper;
    private final BillMapper billMapper;

    @Autowired
    public ProviderServiceImpl(ProviderMapper providerMapper, BillMapper billMapper) {
        this.providerMapper = providerMapper;
        this.billMapper = billMapper;
    }

    @Override
    public boolean add(Provider provider) {

        boolean flag = false;
        try{
            if(providerMapper.add(provider) > 0)
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
            billCount = billMapper.getBillCountByProviderId(delId);
            if(billCount == 0) {
                providerMapper.deleteProviderById(delId);
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
            provider = providerMapper.getProviderById(id);
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
            if(providerMapper.modify(provider) > 0)
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
            providerList = providerMapper.getProviderList(proName,proCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return providerList;
    }
}
