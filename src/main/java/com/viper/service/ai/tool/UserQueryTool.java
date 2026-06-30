package com.viper.service.ai.tool;

import com.viper.controller.dto.UserDTO;
import com.viper.pojo.User;
import com.viper.service.user.UserService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户查询工具。
 * <p>故意只暴露查询接口；UserService 的 add/modify/delete 不在工具白名单内，
 * 因此 AI 物理上无法触发任何写操作。</p>
 */
@Component
public class UserQueryTool {

    private final UserService userService;

    public UserQueryTool(UserService userService) {
        this.userService = userService;
    }

    @Tool(name = "getUserInfo",
            value = "根据用户主键 id 查询用户详细信息（包含姓名、角色、电话、地址等），不会返回密码。" +
                    "当用户问到某个具体员工/账号信息时使用。")
    public Map<String, Object> getUserInfo(@P("用户主键 id") Long userId) {
        ToolGuard.requireLogin();
        User u = userService.getUserById(String.valueOf(userId));
        if (u == null) return Map.of("found", false);
        UserDTO d = UserDTO.fromEntity(u);
        return Map.of(
                "found", true,
                "id", d.getId(),
                "userCode", d.getUserCode(),
                "userName", d.getUserName(),
                "gender", d.getGender() == null ? "" : (d.getGender() == 1 ? "男" : "女"),
                "phone", nullToEmpty(d.getPhone()),
                "address", nullToEmpty(d.getAddress()),
                "roleName", nullToEmpty(d.getUserRoleName())
        );
    }

    @Tool(name = "listUsers",
            value = "分页查询用户列表，可按用户姓名模糊查询与角色 id 过滤。" +
                    "当用户问'有哪些采购员''叫张三的员工'时使用。" +
                    "pageSize 上限 50。返回字段：total 与 records[]。")
    public Map<String, Object> listUsers(
            @P("姓名模糊关键字，可空") String queryName,
            @P("角色 id，0 表示全部；1=管理员 2=采购员 3=销售员") Integer queryUserRole,
            @P("页码，从 1 开始") Integer pageNum,
            @P("每页条数，默认 10，最大 50") Integer pageSize) {

        ToolGuard.requireLogin();
        String name = queryName == null ? "" : queryName;
        int role = queryUserRole == null ? 0 : queryUserRole;
        int page = ToolGuard.clampPageNum(pageNum);
        int size = ToolGuard.clampPageSize(pageSize);

        int total = userService.getUserCount(name, role);
        List<User> users = userService.getUserList(name, role, page, size);
        List<Map<String, Object>> records = users == null ? List.of() :
                users.stream().map(u -> Map.<String, Object>of(
                        "id", u.getId(),
                        "userCode", u.getUserCode(),
                        "userName", u.getUserName(),
                        "phone", nullToEmpty(u.getPhone()),
                        "roleName", nullToEmpty(u.getUserRoleName())
                )).collect(Collectors.toList());

        return Map.of("total", total, "pageNum", page, "pageSize", size, "records", records);
    }

    private static String nullToEmpty(String s) { return s == null ? "" : s; }
}
