package com.viper.controller.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/user/pwdModify")
public class PwdModifyController{

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST})
    public String handleRequest(){
        return "redirect:http://localhost:8080/smbms/jsp/pwdmodify.jsp";
    }
}
