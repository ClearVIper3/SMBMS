package com.viper.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;

/**
 * 数据库异常转译工具。
 * <p>
 * 重新设计后的数据库引入了【唯一约束】与【数据库层外键约束】，当后端执行
 * 新增 / 修改时若违反这些约束，MyBatis-Plus / Spring 会抛出：
 * <ul>
 *     <li>{@link DuplicateKeyException}        —— 唯一键冲突（如员工编码重复）</li>
 *     <li>{@link DataIntegrityViolationException} —— 外键约束冲突（如供应商不存在）</li>
 * </ul>
 * 本工具根据异常信息中携带的【约束名】匹配出对应的友好中文提示，
 * 统一转换为 {@link BusinessException} 抛给上层。
 */
public final class DbExceptionTranslator {

    private DbExceptionTranslator() {
    }

    /**
     * 将 Spring 数据访问异常转换为带友好提示的 {@link BusinessException}。
     * 若不是已知的约束异常，则原样返回 null，由调用方决定如何处理。
     */
    public static BusinessException translate(Exception ex) {
        if (ex == null) {
            return null;
        }
        String msg = rootMessage(ex).toLowerCase();

        // ---------- 唯一约束冲突（编码重复） ----------
        if (ex instanceof DuplicateKeyException || msg.contains("duplicate entry")) {
            if (msg.contains("uk_user_usercode")) {
                return new BusinessException("用户编码（员工唯一标识）已存在，请更换后重试", ex);
            }
            if (msg.contains("uk_provider_procode")) {
                return new BusinessException("供应商编码已存在，请更换后重试", ex);
            }
            if (msg.contains("uk_bill_billcode")) {
                return new BusinessException("订单编码已存在，请更换后重试", ex);
            }
            if (msg.contains("uk_role_rolecode")) {
                return new BusinessException("角色编码已存在，请更换后重试", ex);
            }
            return new BusinessException("数据重复，存在唯一标识冲突，请检查后重试", ex);
        }

        // ---------- 外键约束冲突 ----------
        if (ex instanceof DataIntegrityViolationException || msg.contains("foreign key constraint fails")) {
            // 新增 / 修改时引用了不存在的父记录
            if (msg.contains("fk_user_role")) {
                return new BusinessException("所选用户角色不存在，请重新选择角色", ex);
            }
            if (msg.contains("fk_bill_provider")) {
                return new BusinessException("所选供应商不存在，无法保存订单，请重新选择供应商", ex);
            }
            if (msg.contains("fk_address_user")) {
                return new BusinessException("所属用户不存在，无法保存地址信息", ex);
            }
            // 删除时被子记录引用（RESTRICT）
            if (msg.contains("cannot delete or update a parent row")) {
                return new BusinessException("该记录已被其它数据引用，无法删除", ex);
            }
            return new BusinessException("数据完整性约束冲突：引用了不存在的关联记录，请检查后重试", ex);
        }

        return null;
    }

    /** 递归取最底层异常的信息，外键 / 唯一键的约束名通常在底层 SQL 异常里。 */
    private static String rootMessage(Throwable ex) {
        Throwable cause = ex;
        StringBuilder sb = new StringBuilder();
        while (cause != null) {
            if (cause.getMessage() != null) {
                sb.append(cause.getMessage()).append(' ');
            }
            cause = cause.getCause();
        }
        return sb.toString();
    }
}
