package com.viper.service.ai.tool;

import com.viper.security.UserContext;

/**
 * 工具调用统一护栏：
 *   1) 必须存在登录用户（UserContext），否则拒绝执行 —— 防止匿名调用越权拿数据；
 *   2) 入参做基础边界控制（避免 AI 生成超大 pageSize 导致全表扫描）；
 *   3) 集中点便于未来扩展：审计日志、行级权限（如普通用户只看自己创建的订单）等。
 */
public final class ToolGuard {

    /** 单次工具调用最大返回行数，硬上限 */
    public static final int MAX_PAGE_SIZE = 50;

    private ToolGuard() {}

    /** 工具入口必调，确保当前线程有合法登录上下文 */
    public static UserContext.CurrentUser requireLogin() {
        UserContext.CurrentUser u = UserContext.get();
        if (u == null) {
            throw new IllegalStateException("AI 工具调用必须在已登录用户上下文中执行");
        }
        return u;
    }

    /** 修正分页参数到安全范围，pageSize 受 MAX_PAGE_SIZE 截断 */
    public static int clampPageSize(Integer pageSize) {
        if (pageSize == null || pageSize <= 0) return 10;
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }

    public static int clampPageNum(Integer pageNum) {
        if (pageNum == null || pageNum <= 0) return 1;
        return pageNum;
    }
}
