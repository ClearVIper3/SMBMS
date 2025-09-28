package com.viper.dao.bill;

import com.viper.pojo.Bill;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("billDao")
public class BillDaoImpl implements BillDao{

    private final SqlSessionTemplate sqlSession;

    @Autowired
    public BillDaoImpl(SqlSessionTemplate sqlSession) {
        this.sqlSession = sqlSession;
    }

    @Override
    public int add(Bill bill) throws Exception {
        return sqlSession.getMapper(BillDao.class).add(bill);
    }

    @Override
    public List<Bill> getBillList(Bill bill) throws Exception {
        return sqlSession.getMapper(BillDao.class).getBillList(bill);
    }

    @Override
    public int deleteBillById(String delId) throws Exception {
        return sqlSession.getMapper(BillDao.class).deleteBillById(delId);
    }

    @Override
    public Bill getBillById(String id) throws Exception {
        return sqlSession.getMapper(BillDao.class).getBillById(id);
    }

    @Override
    public int modify(Bill bill) throws Exception {
        return sqlSession.getMapper(BillDao.class).modify(bill);
    }

    @Override
    public int getBillCountByProviderId(String providerId) throws Exception {
        return  sqlSession.getMapper(BillDao.class).getBillCountByProviderId(providerId);
    }
}
