package com.viper.servlet.user;

import com.viper.pojo.User;
import com.viper.service.user.UserService;
import com.viper.utils.Constants;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class LoginServlet extends HttpServlet {

    private UserService userService;

    public void init() throws ServletException {
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        userService = context.getBean("userService", UserService.class);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        System.out.println("LoginServlet--start...");

        //获取账号密码
        String userCode = req.getParameter("userCode");
        String password = req.getParameter("userPassword");

        //和数据库的密码进行比较，调用业务层
        User user = userService.Login(userCode, password);

        if(user != null && user.getUserPassword().equals(password)){//查有此人
            //将此人信息存入Session
            req.getSession().setAttribute(Constants.USER_SESSION, user);
            //跳转到主页
            resp.sendRedirect("jsp/frame.jsp");
        }else {//查无此人 无法登录
            //转发回登录页面，附带error提示信息
            req.setAttribute("error","用户名或密码错误");
            req.getRequestDispatcher("login.jsp").forward(req, resp);//为了保存req设置携带的信息，所以需要转发
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }
}
