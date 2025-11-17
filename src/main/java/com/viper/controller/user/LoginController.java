package com.viper.controller.user;

import com.viper.pojo.User;
import com.viper.service.user.UserService;
import com.viper.utils.Constants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/login.do")
public class LoginController{

    private final UserService userService;

    public LoginController(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST})
    public String handleRequest(
            @RequestParam("userCode") String userCode,
            @RequestParam("userPassword") String password,
            HttpSession session,
            Model model) {

        System.out.println("LoginServlet--start...");

        // 和数据库的密码进行比较，调用业务层
        User user = userService.Login(userCode, password);

        if (user != null && user.getUserPassword().equals(password)) {
            // 查有此人，将此人信息存入Session
            session.setAttribute(Constants.USER_SESSION, user);
            // 重定向到主页
            return "redirect:/frame";
        } else {
            // 查无此人，无法登录
            // 返回登录页面，附带error提示信息
            model.addAttribute("error", "用户名或密码错误");
            return "login";
        }
    }
}
