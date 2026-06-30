package com.viper.service;

import com.viper.exception.BusinessException;
import com.viper.pojo.Bill;
import com.viper.service.bill.BillService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class BillServiceImplTest {

    @Autowired
    private BillService billService;

    @Test
    void getBillList_should_return_all_when_no_filter() {
        Bill query = new Bill();
        query.setProductName("");
        query.setProviderId(0L);
        query.setIsPayment(0);

        List<Bill> list = billService.getBillList(query);
        assertEquals(3, list.size());
        // 关联供应商名（XML 中 join smbms_provider）
        assertNotNull(list.get(0).getProviderName());
    }

    @Test
    void getBillList_should_filter_by_productName_like() {
        Bill query = new Bill();
        query.setProductName("电脑");
        query.setProviderId(0L);
        query.setIsPayment(0);

        List<Bill> list = billService.getBillList(query);
        assertEquals(1, list.size());
        assertEquals("电脑", list.get(0).getProductName());
    }

    @Test
    void getBillList_should_filter_by_provider_and_payment() {
        Bill query = new Bill();
        query.setProductName("");
        query.setProviderId(2L);
        query.setIsPayment(1);

        List<Bill> list = billService.getBillList(query);
        assertEquals(1, list.size());
        assertEquals("B002", list.get(0).getBillCode());
    }

    @Test
    void getBillById_should_join_provider_name() {
        Bill b = billService.getBillById("1");
        assertNotNull(b);
        assertEquals("B001", b.getBillCode());
        assertNotNull(b.getProviderName());
    }

    @Test
    void add_should_insert_new_bill() {
        Bill b = newBill("B999", 1L);
        assertTrue(billService.add(b));
        assertNotNull(b.getId());
        assertEquals("B999", billService.getBillById(b.getId().toString()).getBillCode());
    }

    @Test
    void add_should_throw_business_exception_on_duplicate_billCode() {
        Bill dup = newBill("B001", 1L);
        BusinessException ex = assertThrows(BusinessException.class, () -> billService.add(dup));
        assertNotNull(ex.getMessage());
    }

    @Test
    void add_should_throw_business_exception_on_unknown_provider() {
        Bill bad = newBill("B998", 9999L);
        // providerId 不存在 → 外键约束失败 → 转译为 BusinessException
        assertThrows(BusinessException.class, () -> billService.add(bad));
    }

    @Test
    void modify_should_update_fields() {
        Bill b = new Bill();
        b.setId(1L);
        b.setProductName("电脑-改名");
        b.setProductUnit("台");
        b.setProductCount(new BigDecimal("3.00"));
        b.setTotalPrice(new BigDecimal("18000.00"));
        b.setIsPayment(2);
        b.setProviderId(1L);
        b.setModifyBy(1L);
        b.setModifyDate(new Date());

        assertTrue(billService.modify(b));
        assertEquals("电脑-改名", billService.getBillById("1").getProductName());
    }

    @Test
    void deleteBillById_should_remove_record() {
        Bill b = newBill("BDEL", 1L);
        billService.add(b);
        assertTrue(billService.deleteBillById(b.getId().toString()));
    }

    private static Bill newBill(String code, Long providerId) {
        Bill b = new Bill();
        b.setBillCode(code);
        b.setProductName("测试商品");
        b.setProductUnit("个");
        b.setProductCount(new BigDecimal("1.00"));
        b.setTotalPrice(new BigDecimal("99.99"));
        b.setIsPayment(1);
        b.setProviderId(providerId);
        b.setCreatedBy(1L);
        b.setCreationDate(new Date());
        return b;
    }
}
