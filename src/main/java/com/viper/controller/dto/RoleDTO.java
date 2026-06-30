package com.viper.controller.dto;

import com.viper.pojo.Role;
import lombok.Data;

@Data
public class RoleDTO {
    private Long id;
    private String roleCode;
    private String roleName;

    public static RoleDTO fromEntity(Role r) {
        if (r == null) return null;
        RoleDTO d = new RoleDTO();
        d.id = r.getId();
        d.roleCode = r.getRoleCode();
        d.roleName = r.getRoleName();
        return d;
    }
}
