package com.viper.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * 统一响应结果类
 */
public class Result extends HashMap<String, Object> {
    
    private static final String CODE = "code";
    private static final String MESSAGE = "message";
    private static final String DATA = "data";
    
    public Result() {
    }
    
    public Result(int code, String message) {
        this.put(CODE, code);
        this.put(MESSAGE, message);
    }
    
    public Result(int code, String message, Object data) {
        this.put(CODE, code);
        this.put(MESSAGE, message);
        this.put(DATA, data);
    }
    
    public static Result success() {
        return new Result(200, "操作成功");
    }
    
    public static Result success(String message) {
        return new Result(200, message);
    }
    
    public static Result success(Object data) {
        return new Result(200, "操作成功", data);
    }
    
    public static Result success(String message, Object data) {
        return new Result(200, message, data);
    }
    
    public static Result error() {
        return new Result(500, "操作失败");
    }
    
    public static Result error(String message) {
        return new Result(500, message);
    }
    
    public static Result error(int code, String message) {
        return new Result(code, message);
    }
    
    public Result put(String key, Object value) {
        super.put(key, value);
        return this;
    }
}

