package com.viper.dao.provider;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.viper.pojo.Provider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProviderMapper extends BaseMapper<Provider> {
    /**
     * 通过供应商名称、编码获取供应商列表-模糊查询-providerList
     * @param proName
     * @return
     * @throws Exception
     */
    public List<Provider> getProviderList(@Param("proName") String proName,@Param("proCode") String proCode)throws Exception;
}