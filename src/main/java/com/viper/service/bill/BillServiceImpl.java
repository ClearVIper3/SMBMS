package com.viper.service.bill;

import com.viper.dao.bill.BillMapper;
import com.viper.exception.DbExceptionTranslator;
import com.viper.pojo.Bill;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class BillServiceImpl implements BillService {

    private final BillMapper billMapper;

    public BillServiceImpl(BillMapper billMapper) {
        this.billMapper = billMapper;
    }

    @Override
    public boolean add(Bill bill) {
        return DbExceptionTranslator.execute(
                () -> billMapper.insert(bill) > 0,
                "添加订单失败，请稍后重试");
    }

    @Override
    public List<Bill> getBillList(Bill bill) {
        return billMapper.getBillList(bill);
    }

    @Override
    public boolean deleteBillById(String delId) {
        return billMapper.deleteById(delId) > 0;
    }

    @Override
    public Bill getBillById(String id) {
        return billMapper.getBillById(id);
    }

    @Override
    public boolean modify(Bill bill) {
        return DbExceptionTranslator.execute(
                () -> billMapper.updateById(bill) > 0,
                "修改订单失败，请稍后重试");
    }
}
