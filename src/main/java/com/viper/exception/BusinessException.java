package com.viper.exception;

/**
 * 业务异常：用于把底层数据库约束异常（唯一键冲突、外键冲突等）
 * 转换为带有友好中文提示的异常，向上层（Controller）传递可直接展示给用户的信息。
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
