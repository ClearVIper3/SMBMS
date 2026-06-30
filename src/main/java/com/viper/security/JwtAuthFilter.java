package com.viper.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.viper.utils.Result;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * JWT 鉴权过滤器：仅作用于 /api/** 路由。
 * <p>
 * 设计要点：
 *  - 登录接口 /api/auth/login 与健康检查 /api/health 走白名单；
 *  - 校验失败统一返回 401 JSON，由全局异常处理之外的边界直接落地；
 *  - 解析成功后写入 {@link UserContext}，控制器通过它拿当前用户。
 */
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** 白名单：登录、健康检查等不需要 token */
    private static final Set<String> WHITELIST = Set.of(
            "/api/auth/login",
            "/api/health"
    );

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = pathWithinContext(request);
        // 仅过滤 /api/** ，不影响旧 Thymeleaf 路由
        return !path.startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String path = pathWithinContext(request);
        if (WHITELIST.contains(path)) {
            chain.doFilter(request, response);
            return;
        }

        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            writeUnauthorized(response, "缺少 Authorization Bearer token");
            return;
        }
        String token = auth.substring("Bearer ".length()).trim();
        try {
            Map<String, Object> payload = jwtService.verify(token);
            Long id = Long.valueOf((String) payload.get("sub"));
            String userCode = (String) payload.get("userCode");
            String userName = (String) payload.get("userName");
            Long roleId = payload.get("roleId") == null ? null : ((Number) payload.get("roleId")).longValue();
            UserContext.set(new UserContext.CurrentUser(id, userCode, userName, roleId));

            chain.doFilter(request, response);
        } catch (JwtService.JwtException e) {
            writeUnauthorized(response, e.getMessage());
        } finally {
            UserContext.clear();
        }
    }

    private static String pathWithinContext(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String ctx = request.getContextPath();
        if (ctx != null && !ctx.isEmpty() && uri.startsWith(ctx)) {
            return uri.substring(ctx.length());
        }
        return uri;
    }

    private static void writeUnauthorized(HttpServletResponse response, String reason) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        MAPPER.writeValue(response.getOutputStream(), Result.error(401, "未登录或登录已失效: " + reason));
    }
}
