package com.viper.utils;

/**
 * 统一 API 响应结构。
 * <p>
 * 用不可变 record，序列化后字段顺序稳定（code / message / data），类型安全；
 * 取代了原先继承 HashMap 的写法（无法获取强类型 data、易拼写错 key）。
 */
public record Result<T>(int code, String message, T data) {

    private static final int OK = 200;
    private static final int ERR = 500;

    public static <T> Result<T> success() { return new Result<>(OK, "操作成功", null); }
    public static <T> Result<T> success(T data) { return new Result<>(OK, "操作成功", data); }
    public static <T> Result<T> success(String message, T data) { return new Result<>(OK, message, data); }
    public static Result<String> successMsg(String message) { return new Result<>(OK, message, null); }

    public static <T> Result<T> error() { return new Result<>(ERR, "操作失败", null); }
    public static <T> Result<T> error(String message) { return new Result<>(ERR, message, null); }
    public static <T> Result<T> error(int code, String message) { return new Result<>(code, message, null); }
}
