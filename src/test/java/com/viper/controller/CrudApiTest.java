package com.viper.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 各资源的 CRUD 接口冒烟测试。重点验证：
 *   - JwtAuthFilter 与白名单工作；
 *   - DTO 转换与 Service 复用；
 *   - 业务约束（如 BillCode 唯一、ProviderId 不存在）通过 ApiExceptionHandler 转为 400。
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CrudApiTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;
    private String token;

    @BeforeEach
    void loginAsAdmin() throws Exception {
        MvcResult r = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userCode\":\"admin\",\"userPassword\":\"123456\"}"))
                .andExpect(status().isOk())
                .andReturn();
        Map<?, ?> body = mapper.readValue(r.getResponse().getContentAsString(), Map.class);
        token = (String) ((Map<?, ?>) body.get("data")).get("token");
    }

    // ---------------- Role ----------------
    @Test
    void roles_should_return_seed() throws Exception {
        mvc.perform(get("/api/roles").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(3));
    }

    // ---------------- User ----------------
    @Test
    void user_list_and_get() throws Exception {
        mvc.perform(get("/api/users?pageIndex=1&pageSize=10")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(4))
                .andExpect(jsonPath("$.data.records[0].userPassword").doesNotExist());

        mvc.perform(get("/api/users/1").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userCode").value("U001"));
    }

    @Test
    void user_add_with_duplicate_code_should_400() throws Exception {
        mvc.perform(post("/api/users").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userCode\":\"U001\",\"userName\":\"重复\",\"userPassword\":\"123\",\"userRole\":2}"))
                .andExpect(status().isBadRequest());
    }

    // ---------------- Provider ----------------
    @Test
    void provider_list_and_get() throws Exception {
        mvc.perform(get("/api/providers").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(3));

        mvc.perform(get("/api/providers/1").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.proCode").value("P001"));
    }

    @Test
    void provider_delete_when_referenced_should_400() throws Exception {
        // P001 被 B001 引用，应返回 400 + "该供应商下还有 N 条订单"
        mvc.perform(delete("/api/providers/1").header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    // ---------------- Bill ----------------
    @Test
    void bill_full_crud() throws Exception {
        // list
        mvc.perform(get("/api/bills").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(3));

        // add
        MvcResult added = mvc.perform(post("/api/bills").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"billCode\":\"B900\",\"productName\":\"新商品\",\"productUnit\":\"个\","
                                + "\"productCount\":1.00,\"totalPrice\":9.99,\"isPayment\":1,\"providerId\":1}"))
                .andExpect(status().isOk())
                .andReturn();
        Map<?, ?> body = mapper.readValue(added.getResponse().getContentAsString(), Map.class);
        Integer newId = (Integer) ((Map<?, ?>) body.get("data")).get("id");

        // get
        mvc.perform(get("/api/bills/" + newId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.billCode").value("B900"));

        // modify
        mvc.perform(put("/api/bills/" + newId).header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productName\":\"改名\",\"productUnit\":\"个\","
                                + "\"productCount\":2.00,\"totalPrice\":19.99,\"isPayment\":2,\"providerId\":1}"))
                .andExpect(status().isOk());

        // delete
        mvc.perform(delete("/api/bills/" + newId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void bill_add_with_unknown_provider_should_400() throws Exception {
        mvc.perform(post("/api/bills").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"billCode\":\"B901\",\"productName\":\"x\",\"productUnit\":\"个\","
                                + "\"productCount\":1,\"totalPrice\":1,\"isPayment\":1,\"providerId\":99999}"))
                .andExpect(status().isBadRequest());
    }
}
