package com.viper.exception;

import com.viper.utils.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * 全局 API 异常处理（作用于 {@code com.viper.controller} 下所有 REST 控制器）。
 * <p>
 * 业务异常 / DAO 兜底 / 参数错误 / 兜底未知异常统一翻译成 {@link Result}。
 */
@Order(0)
@RestControllerAdvice(basePackages = "com.viper.controller")
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    /** 业务异常 —— 已是友好提示，直接 400 返回 */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result> handleBusiness(BusinessException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Result.error(400, ex.getMessage()));
    }

    /** DAO 未被 Service 转译的兜底（理论上不应发生） */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Result> handleDb(DataAccessException ex) {
        BusinessException be = DbExceptionTranslator.translate(ex);
        if (be != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Result.error(400, be.getMessage()));
        }
        log.error("未识别的数据库异常", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error(500, "数据访问失败，请稍后重试"));
    }

    /** 参数校验类 —— 必填缺失、类型不匹配、非法参数 */
    @ExceptionHandler({MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class,
            IllegalArgumentException.class})
    public ResponseEntity<Result> handleBadRequest(Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Result.error(400, "参数错误: " + ex.getMessage()));
    }

    /** 兜底 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result> handleAny(Exception ex, HandlerMethod handler) {
        log.error("[API] {} 抛出未处理异常", handler == null ? "?" : handler.getMethod(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error(500, "服务器内部错误"));
    }
}
