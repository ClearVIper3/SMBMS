package com.viper.controller;

import com.viper.controller.dto.PageResponse;
import com.viper.controller.dto.UserDTO;
import com.viper.controller.dto.UserUpsertRequest;
import com.viper.exception.BusinessException;
import com.viper.pojo.User;
import com.viper.security.UserContext;
import com.viper.service.user.UserService;
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

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserApiController {

    private final UserService userService;

    public UserApiController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Result<PageResponse<UserDTO>> list(
            @RequestParam(value = "queryName", required = false, defaultValue = "") String queryName,
            @RequestParam(value = "queryUserRole", required = false, defaultValue = "0") int queryUserRole,
            @RequestParam(value = "pageIndex", required = false, defaultValue = "1") int pageIndex,
            @RequestParam(value = "pageSize", required = false, defaultValue = "5") int pageSize) {

        int total = userService.getUserCount(queryName, queryUserRole);
        List<User> users = userService.getUserList(queryName, queryUserRole, Math.max(pageIndex, 1), pageSize);
        List<UserDTO> records = users == null ? List.of() : users.stream().map(UserDTO::fromEntity).toList();
        return Result.success(PageResponse.of(records, total, pageIndex, pageSize));
    }

    @GetMapping("/{id}")
    public Result<UserDTO> get(@PathVariable("id") String id) {
        User u = userService.getUserById(id);
        if (u == null) throw new BusinessException("用户不存在");
        return Result.success(UserDTO.fromEntity(u));
    }

    @GetMapping("/exists")
    public Result<Boolean> codeExists(@RequestParam("userCode") String userCode) {
        if (!StringUtils.hasLength(userCode)) return Result.success(true);
        return Result.success(userService.selectUserCodeExist(userCode) != null);
    }

    @PostMapping
    public Result<UserDTO> add(@RequestBody UserUpsertRequest req) {
        if (!StringUtils.hasLength(req.getUserCode()) || !StringUtils.hasLength(req.getUserPassword())) {
            throw new BusinessException("用户编码与密码必填");
        }
        User u = req.toNewEntity(UserContext.require().getId());
        if (!userService.add(u)) throw new BusinessException("添加用户失败");
        return Result.success(UserDTO.fromEntity(u));
    }

    @PutMapping("/{id}")
    public Result<String> modify(@PathVariable("id") Long id, @RequestBody UserUpsertRequest req) {
        User u = req.toUpdateEntity(id, UserContext.require().getId());
        if (!userService.modify(u)) throw new BusinessException("修改用户失败");
        return Result.successMsg("修改成功");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable("id") Integer id) {
        if (id == null || id <= 0) throw new BusinessException("非法 id");
        if (!userService.deleteUserById(id)) throw new BusinessException("删除失败：该用户可能被其它数据引用");
        return Result.successMsg("删除成功");
    }
}
