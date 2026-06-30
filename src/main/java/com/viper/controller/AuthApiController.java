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

/** 鉴权 REST 接口：登录、登出、获取当前用户、改密。 */
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
    public Result<LoginResponse> login(@RequestBody LoginRequest req) {
        if (!StringUtils.hasLength(req.getUserCode()) || !StringUtils.hasLength(req.getUserPassword())) {
            throw new BusinessException("用户名或密码不能为空");
        }
        User u = userService.Login(req.getUserCode());
        if (u == null || !PasswordUtil.matches(req.getUserPassword(), u.getUserPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        return Result.success(toLoginResponse(u,
                jwtService.issue(u.getId(), u.getUserCode(), u.getUserName(), u.getUserRole())));
    }

    /**
     * JWT 是无状态的，"登出"只起前端清理 token 的语义占位；
     * 若未来需要服务端强制吊销，可引入黑名单表。
     */
    @PostMapping("/logout")
    public Result<String> logout() {
        return Result.successMsg("已登出");
    }

    @GetMapping("/me")
    public Result<LoginResponse> me() {
        UserContext.CurrentUser u = UserContext.require();
        LoginResponse r = new LoginResponse();
        r.setUserId(u.getId());
        r.setUserCode(u.getUserCode());
        r.setUserName(u.getUserName());
        r.setRoleId(u.getRoleId());
        return Result.success(r);
    }

    @PostMapping("/password")
    public Result<String> modifyPassword(@RequestBody PasswordModifyRequest req) {
        if (!StringUtils.hasLength(req.getNewPassword())) throw new BusinessException("新密码不能为空");
        UserContext.CurrentUser cu = UserContext.require();
        User current = userService.getUserById(String.valueOf(cu.getId()));
        if (current == null || !PasswordUtil.matches(req.getOldPassword(), current.getUserPassword())) {
            throw new BusinessException("原密码错误");
        }
        if (!userService.PasswordModify(cu.getId(), req.getNewPassword())) {
            throw new BusinessException("修改密码失败");
        }
        return Result.successMsg("修改成功，请重新登录");
    }

    private static LoginResponse toLoginResponse(User u, String token) {
        LoginResponse r = new LoginResponse();
        r.setToken(token);
        r.setUserId(u.getId());
        r.setUserCode(u.getUserCode());
        r.setUserName(u.getUserName());
        r.setRoleId(u.getUserRole());
        return r;
    }
}
