package com.viper.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 固化 {@link PasswordUtil} 现状行为：
 *  - BCrypt 加密结果不可逆且每次盐不同；
 *  - matches 既能比对哈希，也能兼容历史明文（迁移期回退）；
 *  - isEncrypted 识别 $2a$/$2b$/$2y$ 三种 BCrypt 前缀。
 */
class PasswordUtilTest {

    @Test
    void encode_should_produce_bcrypt_hash() {
        String hash = PasswordUtil.encode("123456");

        assertNotEquals("123456", hash, "BCrypt 不应返回明文");
        assertTrue(PasswordUtil.isEncrypted(hash), "encode 结果应满足 isEncrypted 判定");
        assertTrue(hash.length() >= 50, "BCrypt 哈希典型长度 60");
    }

    @Test
    void encode_should_use_random_salt() {
        String h1 = PasswordUtil.encode("123456");
        String h2 = PasswordUtil.encode("123456");

        assertNotEquals(h1, h2, "相同明文每次加密结果应不同（随机盐）");
        assertTrue(PasswordUtil.matches("123456", h1));
        assertTrue(PasswordUtil.matches("123456", h2));
    }

    @Test
    void matches_should_verify_correct_password() {
        String hash = PasswordUtil.encode("hello");

        assertTrue(PasswordUtil.matches("hello", hash));
        assertFalse(PasswordUtil.matches("HELLO", hash));
        assertFalse(PasswordUtil.matches("", hash));
    }

    @Test
    void matches_should_fallback_to_plain_text_for_legacy_records() {
        // 兼容历史明文：未加密的密码按字符串相等比较
        assertTrue(PasswordUtil.matches("123456", "123456"));
        assertFalse(PasswordUtil.matches("123456", "654321"));
    }

    @Test
    void matches_should_return_false_for_null_inputs() {
        assertFalse(PasswordUtil.matches(null, "x"));
        assertFalse(PasswordUtil.matches("x", null));
        assertFalse(PasswordUtil.matches(null, null));
    }

    @Test
    void isEncrypted_should_recognize_all_bcrypt_prefixes() {
        assertTrue(PasswordUtil.isEncrypted("$2a$10$abcdefghijabcdefghij.abcdefghijabcdefghijabcdefghijab"));
        assertTrue(PasswordUtil.isEncrypted("$2b$10$xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"));
        assertTrue(PasswordUtil.isEncrypted("$2y$10$yyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy"));
        assertFalse(PasswordUtil.isEncrypted("123456"));
        assertFalse(PasswordUtil.isEncrypted(""));
        assertFalse(PasswordUtil.isEncrypted(null));
    }
}
