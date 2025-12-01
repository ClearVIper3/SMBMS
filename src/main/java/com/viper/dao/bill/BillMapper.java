package com.viper.dao.bill;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.viper.pojo.Bill;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BillMapper extends BaseMapper<Bill> {
    /**
     * 通过查询条件获取供应商列表-模糊查询-getBillList
     * @param bill
     * @return
     * @throws Exception
     */
    List<Bill> getBillList(Bill bill)throws Exception;

    /**
     * 通过billId获取Bill
     * @param id
     * @return
     * @throws Exception
     */
    Bill getBillById(@Param("id") String id)throws Exception;

    /**
     * 根据供应商ID查询订单数量
     * @param providerId
     * @return
     * @throws Exception
     */
    int getBillCountByProviderId(@Param("providerId")String providerId) throws Exception;
}