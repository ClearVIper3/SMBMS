package com.viper.service;

import com.viper.exception.BusinessException;
import com.viper.pojo.Provider;
import com.viper.service.provider.ProviderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class ProviderServiceImplTest {

    @Autowired
    private ProviderService providerService;

    @Test
    void getProviderList_should_return_all_sorted_by_id_when_no_filter() {
        List<Provider> list = providerService.getProviderList("", "");
        assertEquals(3, list.size());
        assertEquals(Long.valueOf(1L), list.get(0).getId());
        assertEquals(Long.valueOf(3L), list.get(2).getId());
    }

    @Test
    void getProviderList_should_support_fuzzy_filter() {
        List<Provider> byName = providerService.getProviderList("华南", "");
        assertEquals(1, byName.size());

        List<Provider> byCode = providerService.getProviderList("", "P00");
        assertEquals(3, byCode.size());
    }

    @Test
    void getProviderById_should_return_seed_record() {
        Provider p = providerService.getProviderById("1");
        assertNotNull(p);
        assertEquals("P001", p.getProCode());
    }

    @Test
    void add_should_insert_new_provider() {
        Provider p = newProvider("P999", "新供应商");
        assertTrue(providerService.add(p));
        assertNotNull(p.getId());
    }

    @Test
    void add_should_throw_on_duplicate_proCode() {
        Provider dup = newProvider("P001", "重复编码");
        BusinessException ex = assertThrows(BusinessException.class, () -> providerService.add(dup));
        assertNotNull(ex.getMessage());
    }

    @Test
    void modify_should_update_fields() {
        Provider p = new Provider();
        p.setId(1L);
        p.setProName("华南供应商-改名");
        p.setProContact("张经理");
        p.setProPhone("020-88888888");
        p.setUserAddress("广州天河-改");
        p.setUserFax("020-88888889");
        p.setProDesc("修改后描述");
        p.setModifyBy(1L);
        p.setModifyDate(new Date());

        assertTrue(providerService.modify(p));
        assertEquals("华南供应商-改名", providerService.getProviderById("1").getProName());
    }

    @Test
    void deleteProviderById_should_return_billCount_when_referenced() {
        // P001 被 B001 引用：现状行为是返回 billCount（>0），表示"该供应商下有订单不可删"
        int result = providerService.deleteProviderById("1");
        assertTrue(result > 0, "被订单引用时应返回订单数（>0），现状为 " + result);
        // 且供应商仍存在
        assertNotNull(providerService.getProviderById("1"));
    }

    @Test
    void deleteProviderById_should_return_zero_when_no_reference() {
        // 先建一个无引用的供应商再删
        Provider p = newProvider("P999", "无引用供应商");
        providerService.add(p);

        int result = providerService.deleteProviderById(p.getId().toString());
        assertEquals(0, result, "无引用时应返回 0 表示删除成功");
    }

    private static Provider newProvider(String code, String name) {
        Provider p = new Provider();
        p.setProCode(code);
        p.setProName(name);
        p.setProContact("联系人");
        p.setProPhone("000-00000000");
        p.setUserAddress("地址");
        p.setUserFax("000-00000001");
        p.setProDesc("描述");
        p.setCreatedBy(1L);
        p.setCreationDate(new Date());
        return p;
    }
}
