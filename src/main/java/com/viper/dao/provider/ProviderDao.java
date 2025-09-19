package com.viper.dao.provider;

import com.viper.pojo.Provider;
import org.apache.ibatis.annotations.Param;

import java.sql.Connection;
import java.util.List;

public interface ProviderDao {
    /**
     * 增加供应商
     * @param provider
     * @return
     * @throws Exception
     */
    public int add(Provider provider)throws Exception;


    /**
     * 通过供应商名称、编码获取供应商列表-模糊查询-providerList
     * @param proName
     * @return
     * @throws Exception
     */
    public List<Provider> getProviderList(@Param("proName") String proName,@Param("proCode") String proCode)throws Exception;

    /**
     * 通过proId删除Provider
     * @param delId
     * @return
     * @throws Exception
     */
    public int deleteProviderById(@Param("delId") String delId)throws Exception;


    /**
     * 通过proId获取Provider
     * @param id
     * @return
     * @throws Exception
     */
    public Provider getProviderById(@Param("id") String id)throws Exception;

    /**
     * 修改用户信息
     */
    public int modify(Provider provider)throws Exception;
}