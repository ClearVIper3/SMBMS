package com.viper.controller;

import com.viper.utils.Constants;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpSession;

@Controller
public class IndexController {

    @GetMapping("/")
    public String index(HttpSession session) {
        Object user = session.getAttribute(Constants.USER_SESSION);
        if (user != null) {
            return "frame";
        } else {
            return "redirect:/login";
        }
    }
}

