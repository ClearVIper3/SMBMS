package com.viper.service.provider;

import com.viper.pojo.Provider;

import java.util.List;

public interface ProviderService {
    /**
     * 增加供应商
     * @param provider
     * @return
     */
    boolean add(Provider provider);


    /**
     * 通过供应商名称、编码获取供应商列表-模糊查询-providerList
     * @param proName
     * @return
     */
    List<Provider> getProviderList(String proName, String proCode);

    /**
     * 通过proId删除Provider
     * @param delId
     * @return
     */
    int deleteProviderById(String delId);


    /**
     * 通过proId获取Provider
     * @param id
     * @return
     */
    Provider getProviderById(String id);

    /**
     * 修改用户信息
     */
    boolean modify(Provider provider);
}