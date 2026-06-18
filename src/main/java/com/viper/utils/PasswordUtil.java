package com.viper.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码加密工具类。
 * <p>
 * 采用 BCrypt 单向哈希算法对用户密码进行加密：
 * <ul>
 *     <li>不可逆：数据库中只存储哈希值，无法还原出明文密码；</li>
 *     <li>自带随机盐：相同明文每次加密结果都不同，可有效抵御彩虹表攻击；</li>
 *     <li>校验时使用 {@link #matches(String, String)} 比对，而非直接比较字符串。</li>
 * </ul>
 * {@link BCryptPasswordEncoder} 线程安全，这里以静态单例方式复用。
 */
public final class PasswordUtil {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    private PasswordUtil() {
    }

    /**
     * 对明文密码进行 BCrypt 加密。
     */
    public static String encode(String rawPassword) {
        return ENCODER.encode(rawPassword);
    }

    /**
     * 校验明文密码与数据库中存储的哈希是否匹配。
     *
     * @param rawPassword     用户输入的明文密码
     * @param encodedPassword 数据库中存储的密码（哈希值，或历史遗留的明文）
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        // 兼容历史明文密码：若库中不是 BCrypt 哈希，则按明文直接比对（用于平滑迁移期间）
        if (!isEncrypted(encodedPassword)) {
            return rawPassword.equals(encodedPassword);
        }
        return ENCODER.matches(rawPassword, encodedPassword);
    }

    /**
     * 判断给定密码字符串是否已是 BCrypt 哈希格式（以 $2a$ / $2b$ / $2y$ 开头）。
     */
    public static boolean isEncrypted(String password) {
        if (password == null) {
            return false;
        }
        return password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$");
    }
}
