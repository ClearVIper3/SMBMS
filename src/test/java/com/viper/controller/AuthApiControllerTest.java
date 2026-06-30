package com.viper.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 阶段 3：REST API 集成测试。
 * 用 MockMvc 端到端跑过 JwtAuthFilter、Controller、Service、Mapper、H2。
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthApiControllerTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;

    @Test
    void login_then_me_roundtrip() throws Exception {
        // 1) 登录
        MvcResult login = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userCode\":\"admin\",\"userPassword\":\"123456\"}"))
                .andExpect(status().isOk())
                .andReturn();
        Map<?, ?> body = mapper.readValue(login.getResponse().getContentAsString(), Map.class);
        assertEquals(200, body.get("code"));
        Map<?, ?> data = (Map<?, ?>) body.get("data");
        String token = (String) data.get("token");
        assertNotNull(token);

        // 2) 访问 /me 需带 token
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void me_without_token_should_return_401() throws Exception {
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void login_with_wrong_password_should_return_400() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userCode\":\"admin\",\"userPassword\":\"wrong\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void health_should_be_open_without_token() throws Exception {
        mvc.perform(get("/api/health")).andExpect(status().isOk());
    }
}
