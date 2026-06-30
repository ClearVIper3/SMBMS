package com.viper.controller;

import com.viper.utils.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthApiController {

    @GetMapping
    public Result health() {
        return Result.success(Map.of("status", "UP"));
    }
}
