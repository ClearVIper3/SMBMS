package com.viper.controller;

import com.viper.controller.dto.PageResponse;
import com.viper.controller.dto.UserDTO;
import com.viper.controller.dto.UserUpsertRequest;
import com.viper.exception.BusinessException;
import com.viper.pojo.User;
import com.viper.security.UserContext;
import com.viper.service.user.UserService;
import com.viper.utils.PasswordUtil;
import com.viper.utils.Result;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/** 用户管理 RESTful API（与旧 UserController 等价，复用 UserService）。 */
@RestController
@RequestMapping("/api/users")
public class UserApiController {

    private final UserService userService;

    public UserApiController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Result list(@RequestParam(value = "queryName", required = false, defaultValue = "") String queryName,
                       @RequestParam(value = "queryUserRole", required = false, defaultValue = "0") int queryUserRole,
                       @RequestParam(value = "pageIndex", required = false, defaultValue = "1") int pageIndex,
                       @RequestParam(value = "pageSize", required = false, defaultValue = "5") int pageSize) {
        int totalCount = userService.getUserCount(queryName, queryUserRole);
        List<User> users = userService.getUserList(queryName, queryUserRole, Math.max(pageIndex, 1), pageSize);
        List<UserDTO> records = users == null ? List.of()
                : users.stream().map(UserDTO::fromEntity).collect(Collectors.toList());
        return Result.success(PageResponse.of(records, totalCount, pageIndex, pageSize));
    }

    @GetMapping("/{id}")
    public Result get(@PathVariable("id") String id) {
        User u = userService.getUserById(id);
        if (u == null) {
            throw new BusinessException("用户不存在");
        }
        return Result.success(UserDTO.fromEntity(u));
    }

    @GetMapping("/exists")
    public Result codeExists(@RequestParam("userCode") String userCode) {
        if (!StringUtils.hasLength(userCode)) {
            return Result.success(true);  // 空视为"已存在"，与旧 /user/ucexist 行为对齐
        }
        return Result.success(userService.selectUserCodeExist(userCode) != null);
    }

    @PostMapping
    public Result add(@RequestBody UserUpsertRequest req) {
        if (!StringUtils.hasLength(req.getUserCode()) || !StringUtils.hasLength(req.getUserPassword())) {
            throw new BusinessException("用户编码与密码必填");
        }
        UserContext.CurrentUser cu = UserContext.require();

        User u = new User();
        u.setUserCode(req.getUserCode());
        u.setUserName(req.getUserName());
        u.setUserPassword(PasswordUtil.encode(req.getUserPassword()));
        u.setGender(req.getGender());
        u.setBirthday(req.getBirthday());
        u.setPhone(req.getPhone());
        u.setAddress(req.getAddress());
        u.setUserRole(req.getUserRole());
        u.setCreatedBy(cu.getId());
        u.setCreationDate(new Date());

        boolean ok = userService.add(u);
        return ok ? Result.success(UserDTO.fromEntity(u)) : Result.error("添加用户失败");
    }

    @PutMapping("/{id}")
    public Result modify(@PathVariable("id") Long id, @RequestBody UserUpsertRequest req) {
        UserContext.CurrentUser cu = UserContext.require();

        // 与旧 /user/modify 一致：不修改 userCode / userPassword
        User u = new User();
        u.setId(id);
        u.setUserName(req.getUserName());
        u.setGender(req.getGender());
        u.setBirthday(req.getBirthday());
        u.setPhone(req.getPhone());
        u.setAddress(req.getAddress());
        u.setUserRole(req.getUserRole());
        u.setModifyBy(cu.getId());
        u.setModifyDate(new Date());

        boolean ok = userService.modify(u);
        return ok ? Result.success("修改成功") : Result.error("修改失败");
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable("id") Integer id) {
        if (id == null || id <= 0) {
            throw new BusinessException("非法 id");
        }
        boolean ok = userService.deleteUserById(id);
        return ok ? Result.success("删除成功") : Result.error("删除失败");
    }
}
