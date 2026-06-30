package com.viper.service.provider;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.viper.dao.bill.BillMapper;
import com.viper.dao.provider.ProviderMapper;
import com.viper.exception.DbExceptionTranslator;
import com.viper.pojo.Provider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
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
        return DbExceptionTranslator.execute(
                () -> providerMapper.insert(provider) > 0,
                "添加供应商失败，请稍后重试");
    }

    /**
     * 返回值约定：
     *   0  删除成功
     *   >0 该供应商下还有 N 条订单，不可删除（值 = N）
     *   -1 其它失败
     * <p>历史接口签名，保持兼容；新增接口请使用 {@link #removeByIdSafely(String)}。
     */
    @Override
    public int deleteProviderById(String delId) {
        try {
            int billCount = billMapper.getBillCountByProviderId(delId);
            if (billCount == 0) {
                providerMapper.deleteById(delId);
            }
            return billCount;
        } catch (Exception ex) {
            return -1;
        }
    }

    @Override
    public Provider getProviderById(String id) {
        return providerMapper.selectById(id);
    }

    @Override
    public boolean modify(Provider provider) {
        return DbExceptionTranslator.execute(
                () -> providerMapper.updateById(provider) > 0,
                "修改供应商失败，请稍后重试");
    }

    @Override
    public List<Provider> getProviderList(String proName, String proCode) {
        QueryWrapper<Provider> wrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(proName)) wrapper.like("proName", proName);
        if (StringUtils.isNotBlank(proCode)) wrapper.like("proCode", proCode);
        wrapper.orderByAsc("id");
        return providerMapper.selectList(wrapper);
    }
}
