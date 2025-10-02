package com.viper.controller.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
@RequestMapping("/user/pwdModify")
public class PwdModifyController{

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST})
    public void handleRequest(
            HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException{
        response.sendRedirect(request.getContextPath() + "/jsp/pwdmodify.jsp");
    }
}
