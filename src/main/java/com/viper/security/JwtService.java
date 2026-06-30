package com.viper.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 最小化 JWT（HS256）实现。
 * <p>
 * 故意不引入第三方 JWT 库（jjwt / nimbus-jose 等），原因：
 *  1. 业务只需要"签发-校验"双操作，特征极简，没必要为此引入数百 KB 依赖与传递依赖；
 *  2. 算法走 JDK 自带 {@link Mac}，无平台依赖；
 *  3. 实现透明，安全审计直接看本类即可；
 *  4. 阶段4新增的 Vue 前端只需 Bearer Token 校验，本类已完全覆盖。
 * <p>
 * Token 结构：base64url(header).base64url(payload).base64url(hmacSha256(header.payload, secret))
 */
@Component
public class JwtService {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();
    private static final String ALGO = "HmacSHA256";

    private final byte[] secret;
    private final long ttlSeconds;

    public JwtService(
            // 生产请通过环境变量 SMBMS_JWT_SECRET 注入≥32字节随机串；本地默认值仅用于开发
            @Value("${smbms.jwt.secret:please-change-this-very-long-dev-only-secret-key-32+bytes}") String secret,
            @Value("${smbms.jwt.ttl-seconds:7200}") long ttlSeconds
    ) {
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException(
                    "JWT secret 必须至少 32 字节（HS256 要求），当前长度=" + secret.length() +
                            "。请通过环境变量 SMBMS_JWT_SECRET 设置。");
        }
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.ttlSeconds = ttlSeconds;
    }

    /**
     * 签发 token。
     * @param userId   主键
     * @param userCode 用户编码（用于审计）
     * @param userName 显示名
     * @param roleId   角色 id（鉴权用）
     */
    public String issue(Long userId, String userCode, String userName, Long roleId) {
        long now = System.currentTimeMillis() / 1000L;

        Map<String, Object> header = new LinkedHashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", String.valueOf(userId));
        payload.put("userCode", userCode);
        payload.put("userName", userName);
        payload.put("roleId", roleId);
        payload.put("iat", now);
        payload.put("exp", now + ttlSeconds);

        String headerB64 = URL_ENCODER.encodeToString(toJson(header));
        String payloadB64 = URL_ENCODER.encodeToString(toJson(payload));
        String signingInput = headerB64 + "." + payloadB64;
        String sigB64 = URL_ENCODER.encodeToString(hmacSha256(signingInput.getBytes(StandardCharsets.UTF_8)));

        return signingInput + "." + sigB64;
    }

    /**
     * 校验并解析 token。
     * @return 解析后的 payload；签名失败/过期/格式错误均抛 {@link JwtException}
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> verify(String token) {
        if (token == null || token.isEmpty()) {
            throw new JwtException("token 为空");
        }
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new JwtException("token 结构错误");
        }
        String signingInput = parts[0] + "." + parts[1];
        byte[] expectedSig = hmacSha256(signingInput.getBytes(StandardCharsets.UTF_8));
        byte[] actualSig;
        try {
            actualSig = URL_DECODER.decode(parts[2]);
        } catch (IllegalArgumentException e) {
            throw new JwtException("token 签名 base64 解码失败", e);
        }
        // 恒定时间比较，防时序攻击
        if (!MessageDigest.isEqual(expectedSig, actualSig)) {
            throw new JwtException("token 签名不匹配");
        }

        Map<String, Object> payload;
        try {
            payload = MAPPER.readValue(URL_DECODER.decode(parts[1]), Map.class);
        } catch (Exception e) {
            throw new JwtException("token payload 解析失败", e);
        }

        Object exp = payload.get("exp");
        if (!(exp instanceof Number)) {
            throw new JwtException("token 缺少 exp 字段");
        }
        long expSec = ((Number) exp).longValue();
        if (System.currentTimeMillis() / 1000L >= expSec) {
            throw new JwtException("token 已过期");
        }
        return payload;
    }

    // ---------- helpers ----------

    private byte[] hmacSha256(byte[] data) {
        try {
            Mac mac = Mac.getInstance(ALGO);
            mac.init(new SecretKeySpec(secret, ALGO));
            return mac.doFinal(data);
        } catch (Exception e) {
            throw new IllegalStateException("HMAC 计算失败", e);
        }
    }

    private static byte[] toJson(Object o) {
        try {
            return MAPPER.writeValueAsBytes(o);
        } catch (Exception e) {
            throw new IllegalStateException("JSON 序列化失败", e);
        }
    }

    /** JWT 校验相关异常。 */
    public static class JwtException extends RuntimeException {
        public JwtException(String msg) { super(msg); }
        public JwtException(String msg, Throwable cause) { super(msg, cause); }
    }
}
