package com.viper.service.bill;

import com.viper.dao.bill.BillDao;
import com.viper.pojo.Bill;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("billService")
public class BillServiceImpl implements BillService{

    private final BillDao billDao;

    //业务层都会调用dao层，所以我们要引入Dao层；
    @Autowired
    public BillServiceImpl(BillDao billDao) {
        this.billDao = billDao;
    }

    public boolean add(Bill bill) {

        boolean flag = false;
        try {
            int updateRows = billDao.add(bill);
            System.out.println("dao--------修改行数 " + updateRows);
            if(updateRows > 0){
                flag = true;
                System.out.println("add success!");
            }else{
                System.out.println("add failed!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("rollback==================");
        }
        return flag;
    }

    public List<Bill> getBillList(Bill bill) {

        List<Bill> billList = new ArrayList<>();
        System.out.println("query productName ---- > " + bill.getProductName());
        System.out.println("query providerId ---- > " + bill.getProviderId());
        System.out.println("query isPayment ---- > " + bill.getIsPayment());
        try {
            billList=billDao.getBillList(bill);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return billList;
    }

    public boolean deleteBillById(String delId) {

        boolean flag=false;
        int delNum;
        try {
            delNum=billDao.deleteBillById(delId);
            if(delNum>0){
                flag=true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    public Bill getBillById(String id) {

        Bill bill = new Bill();
        try {
            bill = billDao.getBillById(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bill;
    }

    public boolean modify(Bill bill) {

        boolean flag=false;
        int modifyNum;
        try {
            modifyNum=billDao.modify(bill);
            if(modifyNum>0){
                flag=true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }
}