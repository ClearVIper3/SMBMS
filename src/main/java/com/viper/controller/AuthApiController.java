package com.viper.controller;

import com.viper.controller.dto.LoginRequest;
import com.viper.controller.dto.LoginResponse;
import com.viper.controller.dto.PasswordModifyRequest;
import com.viper.exception.BusinessException;
import com.viper.pojo.User;
import com.viper.security.JwtService;
import com.viper.security.UserContext;
import com.viper.service.user.UserService;
import com.viper.utils.PasswordUtil;
import com.viper.utils.Result;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 鉴权相关接口（登录、登出、获取当前用户、改密）。
 * <p>
 * 旧 LoginController/LogoutController 保持不变，作为 Thymeleaf 入口；
 * 本类是面向 Vue 前端的 REST 版本，复用同一 Service 层确保业务行为一致。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    private final UserService userService;
    private final JwtService jwtService;

    public AuthApiController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public Result login(@RequestBody LoginRequest req) {
        if (!StringUtils.hasLength(req.getUserCode()) || !StringUtils.hasLength(req.getUserPassword())) {
            throw new BusinessException("用户名或密码不能为空");
        }
        User u = userService.Login(req.getUserCode());
        if (u == null || !PasswordUtil.matches(req.getUserPassword(), u.getUserPassword())) {
            // 与旧 LoginController "用户名或密码错误" 保持一致的提示
            throw new BusinessException("用户名或密码错误");
        }
        String token = jwtService.issue(u.getId(), u.getUserCode(), u.getUserName(), u.getUserRole());

        LoginResponse resp = new LoginResponse();
        resp.setToken(token);
        resp.setUserId(u.getId());
        resp.setUserCode(u.getUserCode());
        resp.setUserName(u.getUserName());
        resp.setRoleId(u.getUserRole());
        return Result.success(resp);
    }

    /**
     * JWT 是无状态的，"登出"接口实际只起前端清理 token 的语义占位。
     * 真正的失效需要前端丢弃 token；如未来需要服务端强制吊销，可引入黑名单表。
     */
    @PostMapping("/logout")
    public Result logout() {
        return Result.success("已登出");
    }

    @GetMapping("/me")
    public Result me() {
        UserContext.CurrentUser u = UserContext.require();
        LoginResponse resp = new LoginResponse();
        resp.setUserId(u.getId());
        resp.setUserCode(u.getUserCode());
        resp.setUserName(u.getUserName());
        resp.setRoleId(u.getRoleId());
        return Result.success(resp);
    }

    @PostMapping("/password")
    public Result modifyPassword(@RequestBody PasswordModifyRequest req) {
        UserContext.CurrentUser cu = UserContext.require();
        if (!StringUtils.hasLength(req.getNewPassword())) {
            throw new BusinessException("新密码不能为空");
        }
        // 校验旧密码（与旧 /user/pwdmodify/check + /user/pwdmodify 两步合并）
        User current = userService.getUserById(String.valueOf(cu.getId()));
        if (current == null || !PasswordUtil.matches(req.getOldPassword(), current.getUserPassword())) {
            throw new BusinessException("原密码错误");
        }
        boolean ok = userService.PasswordModify(cu.getId(), req.getNewPassword());
        return ok ? Result.success("修改成功，请重新登录") : Result.error("修改密码失败");
    }
}
