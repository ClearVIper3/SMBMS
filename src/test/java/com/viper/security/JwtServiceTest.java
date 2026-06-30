package com.viper.security;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {

    private static final String SECRET = "unit-test-secret-key-must-be-at-least-32-bytes-long-padding";

    @Test
    void issue_and_verify_roundtrip() {
        JwtService svc = new JwtService(SECRET, 3600);
        String token = svc.issue(1L, "admin", "管理员", 1L);
        Map<String, Object> payload = svc.verify(token);
        assertEquals("1", payload.get("sub"));
        assertEquals("admin", payload.get("userCode"));
        assertEquals("管理员", payload.get("userName"));
        assertEquals(1, ((Number) payload.get("roleId")).intValue());
    }

    @Test
    void verify_should_reject_tampered_token() {
        JwtService svc = new JwtService(SECRET, 3600);
        String token = svc.issue(1L, "admin", "管理员", 1L);
        // 把最后一个字符改掉，破坏签名
        String tampered = token.substring(0, token.length() - 1) + (token.endsWith("A") ? "B" : "A");
        assertThrows(JwtService.JwtException.class, () -> svc.verify(tampered));
    }

    @Test
    void verify_should_reject_expired_token() throws Exception {
        JwtService svc = new JwtService(SECRET, 1);
        String token = svc.issue(1L, "admin", "管理员", 1L);
        Thread.sleep(1100);
        assertThrows(JwtService.JwtException.class, () -> svc.verify(token));
    }

    @Test
    void construct_should_reject_short_secret() {
        assertThrows(IllegalStateException.class, () -> new JwtService("too-short", 3600));
    }
}
