package com.viper.exception;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;

import java.util.function.Supplier;

/**
 * 数据库异常转译工具。
 * <p>
 * 把底层约束异常（唯一键 / 外键）翻译成带友好中文提示的 {@link BusinessException}。
 * 同时提供 {@link #execute(Supplier, String)} 抽出三个 Service 中重复的 try-catch 样板。
 */
public final class DbExceptionTranslator {

    private DbExceptionTranslator() {}

    /**
     * 包装一段可能抛 DataAccessException 的数据访问代码，
     * 把 DB 约束异常自动翻译为 BusinessException；其余按 fallbackMsg 兜底。
     *
     * <pre>
     *   return DbExceptionTranslator.execute(
     *       () -> userMapper.insert(user) > 0,
     *       "添加用户失败，请稍后重试");
     * </pre>
     */
    public static <T> T execute(Supplier<T> action, String fallbackMsg) {
        try {
            return action.get();
        } catch (DataAccessException e) {
            BusinessException be = translate(e);
            throw be != null ? be : new BusinessException(fallbackMsg, e);
        }
    }

    /** 将 Spring 数据访问异常翻译为 BusinessException；无法识别返回 null。 */
    public static BusinessException translate(Exception ex) {
        if (ex == null) return null;
        String msg = rootMessage(ex).toLowerCase();

        // ---------- 唯一约束冲突 ----------
        if (ex instanceof DuplicateKeyException || msg.contains("duplicate entry")) {
            if (msg.contains("uk_user_usercode"))     return new BusinessException("用户编码（员工唯一标识）已存在，请更换后重试", ex);
            if (msg.contains("uk_provider_procode"))  return new BusinessException("供应商编码已存在，请更换后重试", ex);
            if (msg.contains("uk_bill_billcode"))     return new BusinessException("订单编码已存在，请更换后重试", ex);
            if (msg.contains("uk_role_rolecode"))     return new BusinessException("角色编码已存在，请更换后重试", ex);
            return new BusinessException("数据重复，存在唯一标识冲突，请检查后重试", ex);
        }

        // ---------- 外键约束冲突 ----------
        if (ex instanceof DataIntegrityViolationException || msg.contains("foreign key constraint fails")) {
            if (msg.contains("fk_user_role"))         return new BusinessException("所选用户角色不存在，请重新选择角色", ex);
            if (msg.contains("fk_bill_provider"))     return new BusinessException("所选供应商不存在，无法保存订单，请重新选择供应商", ex);
            if (msg.contains("fk_address_user"))      return new BusinessException("所属用户不存在，无法保存地址信息", ex);
            if (msg.contains("cannot delete or update a parent row"))
                return new BusinessException("该记录已被其它数据引用，无法删除", ex);
            return new BusinessException("数据完整性约束冲突：引用了不存在的关联记录，请检查后重试", ex);
        }
        return null;
    }

    /** 递归取最底层异常信息，约束名通常在底层 SQL 异常里。 */
    private static String rootMessage(Throwable ex) {
        Throwable cause = ex;
        StringBuilder sb = new StringBuilder();
        while (cause != null) {
            if (cause.getMessage() != null) sb.append(cause.getMessage()).append(' ');
            cause = cause.getCause();
        }
        return sb.toString();
    }
}
