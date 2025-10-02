package com.viper.dao.bill;

import com.viper.pojo.Bill;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BillMapper {
    /**
     * 增加订单
     * @param bill
     * @return
     * @throws Exception
     */
    public int add(Bill bill)throws Exception;


    /**
     * 通过查询条件获取供应商列表-模糊查询-getBillList
     * @param bill
     * @return
     * @throws Exception
     */
    public List<Bill> getBillList(Bill bill)throws Exception;

    /**
     * 通过delId删除Bill
     * @param delId
     * @return
     * @throws Exception
     */
    public int deleteBillById(@Param("delId") String delId)throws Exception;


    /**
     * 通过billId获取Bill
     * @param id
     * @return
     * @throws Exception
     */
    public Bill getBillById(@Param("id") String id)throws Exception;

    /**
     * 修改订单信息
     * @param bill
     * @return
     * @throws Exception
     */
    public int modify(Bill bill)throws Exception;

    /**
     * 根据供应商ID查询订单数量
     * @param providerId
     * @return
     * @throws Exception
     */
    public int getBillCountByProviderId(@Param("providerId")String providerId)throws Exception;
}