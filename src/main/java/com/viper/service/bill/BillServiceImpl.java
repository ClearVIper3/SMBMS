package com.viper.service.bill;

import com.viper.dao.bill.BillMapper;
import com.viper.pojo.Bill;
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
        return billMapper.insert(bill) > 0;
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
        return billMapper.updateById(bill) > 0;
    }
}