package com.viper.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/frame")
public class FrameController {

    @GetMapping
    public String frame() {
        return "frame";
    }
}

