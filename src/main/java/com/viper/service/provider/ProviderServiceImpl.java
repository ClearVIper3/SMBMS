package com.viper.service.provider;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.viper.dao.bill.BillMapper;
import com.viper.dao.provider.ProviderMapper;
import com.viper.exception.BusinessException;
import com.viper.exception.DbExceptionTranslator;
import com.viper.pojo.Provider;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("providerService")
@Transactional
public class ProviderServiceImpl implements ProviderService {

    private final ProviderMapper providerMapper;
    private final BillMapper billMapper;

    public ProviderServiceImpl(ProviderMapper providerMapper, BillMapper billMapper) {
        this.providerMapper = providerMapper;
        this.billMapper = billMapper;
    }

    @Override
    public boolean add(Provider provider) {
        try {
            return providerMapper.insert(provider) > 0;
        } catch (DataAccessException e) {
            // 捕获供应商编码重复等唯一约束冲突
            BusinessException be = DbExceptionTranslator.translate(e);
            throw be != null ? be : new BusinessException("添加供应商失败，请稍后重试", e);
        }
    }

    @Override
    public int deleteProviderById(String delId) {
        int billCount;

        try{
            billCount = billMapper.getBillCountByProviderId(delId);
            if(billCount == 0) {
                providerMapper.deleteById(delId);
            }
        } catch(Exception ex) {
            ex.printStackTrace();
            billCount = -1;
        }
        return billCount;
    }

    @Override
    public Provider getProviderById(String id) {
        return providerMapper.selectById(id);
    }

    @Override
    public boolean modify(Provider provider) {
        try {
            return providerMapper.updateById(provider) > 0;
        } catch (DataAccessException e) {
            BusinessException be = DbExceptionTranslator.translate(e);
            throw be != null ? be : new BusinessException("修改供应商失败，请稍后重试", e);
        }
    }

    @Override
    public List<Provider> getProviderList(String proName, String proCode) {

        QueryWrapper<Provider> wrapper = new QueryWrapper<>();

        if (StringUtils.isNotBlank(proName)) {
            wrapper.like("proName", proName);
        }
        if (StringUtils.isNotBlank(proCode)) {
            wrapper.like("proCode", proCode);
        }

        // 添加排序
        wrapper.orderByAsc("id");

        return providerMapper.selectList(wrapper);
    }
}
