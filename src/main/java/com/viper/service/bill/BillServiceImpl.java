package com.viper.service.bill;

import com.viper.dao.bill.BillDao;
import com.viper.pojo.Bill;
import com.viper.utils.MybatisUtils;
import org.apache.ibatis.session.SqlSession;

import java.util.ArrayList;
import java.util.List;

public class BillServiceImpl implements BillService{

    private SqlSession  sqlSession;
    private BillDao billDao;

    //业务层都会调用dao层，所以我们要引入Dao层；
    public BillServiceImpl(){
    }

    public boolean add(Bill bill) {
        sqlSession = MybatisUtils.getSqlSession();
        billDao = sqlSession.getMapper(BillDao.class);
        boolean flag = false;
        try {
            int updateRows = billDao.add(bill);
            System.out.println("dao--------修改行数 " + updateRows);
            sqlSession.commit();
            if(updateRows > 0){
                flag = true;
                System.out.println("add success!");
            }else{
                System.out.println("add failed!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("rollback==================");
            sqlSession.rollback();//失败就回滚
        }finally{
            sqlSession.close();
        }
        return flag;
    }

    public List<Bill> getBillList(Bill bill) {
        sqlSession = MybatisUtils.getSqlSession();
        billDao = sqlSession.getMapper(BillDao.class);

        List<Bill> billList = new ArrayList<Bill>();
        System.out.println("query productName ---- > " + bill.getProductName());
        System.out.println("query providerId ---- > " + bill.getProviderId());
        System.out.println("query isPayment ---- > " + bill.getIsPayment());
        try {
            billList=billDao.getBillList(bill);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            sqlSession.close();
        }
        return billList;
    }

    public boolean deleteBillById(String delId) {
        sqlSession = MybatisUtils.getSqlSession();
        billDao = sqlSession.getMapper(BillDao.class);

        boolean flag=false;
        int delNum=0;
        try {
            delNum=billDao.deleteBillById(delId);
            sqlSession.commit();
            if(delNum>0){
                flag=true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            sqlSession.close();
        }
        return flag;
    }

    public Bill getBillById(String id) {
        sqlSession = MybatisUtils.getSqlSession();
        billDao = sqlSession.getMapper(BillDao.class);

        Bill bill = new Bill();
        try {
            bill = billDao.getBillById(id);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            sqlSession.close();
        }
        return bill;
    }

    public boolean modify(Bill bill) {
        sqlSession = MybatisUtils.getSqlSession();
        billDao = sqlSession.getMapper(BillDao.class);

        Boolean flag=false;
        int modifyNum=0;
        try {
            modifyNum=billDao.modify(bill);
            sqlSession.commit();
            if(modifyNum>0){
                flag=true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            sqlSession.close();
        }
        return flag;
    }
}