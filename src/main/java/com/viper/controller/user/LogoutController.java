package com.viper.controller.user;

import com.viper.utils.Constants;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/user/logout")
public class LogoutController{

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST})
    public String handleRequest(HttpSession session) {
        // 移除session中的用户信息
        session.removeAttribute(Constants.USER_SESSION);

        // 重定向到登录页面
        return "redirect:/login";
    }
}
