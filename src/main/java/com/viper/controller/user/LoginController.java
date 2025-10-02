package com.viper.controller.user;

import com.viper.pojo.User;
import com.viper.service.user.UserService;
import com.viper.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
@RequestMapping("/login.do")
public class LoginController{

    private final UserService userService;

    @Autowired
    public LoginController(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST})
    public void handleRequest(
            @RequestParam("userCode") String userCode,
            @RequestParam("userPassword") String password,
            HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException{

        System.out.println("LoginServlet--start...");

        //和数据库的密码进行比较，调用业务层
        User user = userService.Login(userCode, password);

        if(user != null && user.getUserPassword().equals(password)){//查有此人
            //将此人信息存入Session
            request.getSession().setAttribute(Constants.USER_SESSION, user);
            //跳转到主页
            response.sendRedirect("jsp/frame.jsp");
        }else {//查无此人 无法登录
            //转发回登录页面，附带error提示信息
            request.setAttribute("error","用户名或密码错误");
            request.getRequestDispatcher("login.jsp").forward(request, response);//为了保存req设置携带的信息，所以需要转发
        }
    }
}
