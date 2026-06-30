package com.viper.security;

/**
 * 请求线程内的当前用户上下文。
 * <p>
 * 由 {@link JwtAuthFilter} 在每次 /api/** 请求开始时设置，请求结束时清理。
 * 用 ThreadLocal 而非把 Principal 挂在 SecurityContext 的原因：
 * 本项目未引入完整的 Spring Security，自己维护更轻量、避免引入半套安全框架。
 */
public final class UserContext {

    private static final ThreadLocal<CurrentUser> HOLDER = new ThreadLocal<>();

    private UserContext() {}

    public static void set(CurrentUser user) {
        HOLDER.set(user);
    }

    public static CurrentUser get() {
        return HOLDER.get();
    }

    /** 必须存在；不存在则抛出运行时异常（接口约定要求已登录） */
    public static CurrentUser require() {
        CurrentUser u = HOLDER.get();
        if (u == null) {
            throw new IllegalStateException("当前线程无登录用户上下文");
        }
        return u;
    }

    public static void clear() {
        HOLDER.remove();
    }

    /** 不可变的当前用户视图，只保留 API 需要的字段，避免泄露密码等敏感信息。 */
    public static final class CurrentUser {
        private final Long id;
        private final String userCode;
        private final String userName;
        private final Long roleId;

        public CurrentUser(Long id, String userCode, String userName, Long roleId) {
            this.id = id;
            this.userCode = userCode;
            this.userName = userName;
            this.roleId = roleId;
        }

        public Long getId() { return id; }
        public String getUserCode() { return userCode; }
        public String getUserName() { return userName; }
        public Long getRoleId() { return roleId; }
    }
}
