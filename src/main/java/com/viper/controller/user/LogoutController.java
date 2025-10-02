package com.viper.controller.user;

import com.viper.utils.Constants;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
@RequestMapping("/user/logout")
public class LogoutController{

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST})
    public void handleRequest(
            HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException{
        request.getSession().removeAttribute(Constants.USER_SESSION);

        response.sendRedirect(request.getContextPath() + "/login.jsp");
    }
}
