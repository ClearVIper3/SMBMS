package com.viper.service.bill;

import com.viper.dao.bill.BillMapper;
import com.viper.exception.BusinessException;
import com.viper.exception.DbExceptionTranslator;
import com.viper.pojo.Bill;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service("billService")
@Transactional
public class BillServiceImpl implements BillService{

    //业务层都会调用dao层，所以我们要引入Dao层
    private final BillMapper billMapper;

    public BillServiceImpl(BillMapper billMapper) {
        this.billMapper = billMapper;
    }

    public boolean add(Bill bill) {
        try {
            return billMapper.insert(bill) > 0;
        } catch (DataAccessException e) {
            // 捕获订单编码重复（唯一约束）、供应商不存在（外键约束）等异常
            BusinessException be = DbExceptionTranslator.translate(e);
            throw be != null ? be : new BusinessException("添加订单失败，请稍后重试", e);
        }
    }

    public List<Bill> getBillList(Bill bill) {

        List<Bill> billList = new ArrayList<>();
        try {
            billList= billMapper.getBillList(bill);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return billList;
    }

    public boolean deleteBillById(String delId) {
        return billMapper.deleteById(delId) > 0;
    }

    public Bill getBillById(String id) {
        Bill bill = new Bill();
        try {
            bill = billMapper.getBillById(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bill;
    }

    public boolean modify(Bill bill) {
        try {
            return billMapper.updateById(bill) > 0;
        } catch (DataAccessException e) {
            BusinessException be = DbExceptionTranslator.translate(e);
            throw be != null ? be : new BusinessException("修改订单失败，请稍后重试", e);
        }
    }
}