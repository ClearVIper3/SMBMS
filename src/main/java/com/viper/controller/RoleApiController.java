package com.viper.controller;

import com.viper.controller.dto.RoleDTO;
import com.viper.service.role.RoleService;
import com.viper.utils.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/roles")
public class RoleApiController {

    private final RoleService roleService;

    public RoleApiController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public Result list() {
        return Result.success(roleService.getRoleList().stream()
                .map(RoleDTO::fromEntity).collect(Collectors.toList()));
    }
}
