package com.viper.controller.user;

import com.mysql.cj.util.StringUtils;
import com.viper.pojo.Role;
import com.viper.pojo.User;
import com.viper.service.role.RoleService;
import com.viper.service.user.UserService;
import com.viper.utils.Constants;
import com.viper.utils.PageSupport;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Controller
@RequestMapping("/user")
public class UserController{

    private final RoleService roleService;
    private final UserService userService;

    public UserController(RoleService roleService, UserService userService) {
        this.roleService = roleService;
        this.userService = userService;
    }

    @PostMapping("/pwdmodify")
    public String updateUser(
            @RequestParam("newpassword") String newPassword,
            @RequestParam(value = "oldpassword", required = false) String oldPassword,
            HttpSession session,
            Model model) {

        Object attribute = session.getAttribute(Constants.USER_SESSION);
        Boolean flag;

        if (attribute != null && !StringUtils.isNullOrEmpty(newPassword)) {
            flag = userService.PasswordModify(((User) attribute).getId(), newPassword);

            if (flag) {
                model.addAttribute("message", "修改密码成功，请退出，使用新密码登录");
                session.removeAttribute(Constants.USER_SESSION);
            } else {
                model.addAttribute("message", "修改密码失败");
            }
        } else {
            model.addAttribute("message", "新密码有问题");
        }

        return "user/pwdmodify";
    }

    @PostMapping("/pwdmodify/check")
    @ResponseBody
    public HashMap<String, String> pwdModify(
            @RequestParam("oldpassword") String oldPassword,
            HttpSession session) {

        Object o = session.getAttribute(Constants.USER_SESSION);
        HashMap<String, String> resultMap = new HashMap<>();

        if (o == null) {
            resultMap.put("result", "sessionerror");
        } else if (StringUtils.isNullOrEmpty(oldPassword)) {
            resultMap.put("result", "error");
        } else {
            String userPassword = ((User) o).getUserPassword();
            if (userPassword.equals(oldPassword)) {
                resultMap.put("result", "true");
            } else {
                resultMap.put("result", "false");
            }
        }

        return resultMap;
    }

    @GetMapping("/list")
    public String query(
            @RequestParam(value = "queryname", required = false) String queryUserName,
            @RequestParam(value = "queryUserRole", required = false) String temp,
            @RequestParam(value = "pageIndex", required = false) String pageIndex,
            Model model) {

        // 处理参数
        if (queryUserName == null) {
            queryUserName = "";
        }

        int queryUserRole = 0;
        if (temp != null && !temp.equals("")) {
            queryUserRole = Integer.parseInt(temp);
        }

        int pageSize = 5;
        int currentPageNo = 1;
        if (pageIndex != null) {
            currentPageNo = Integer.parseInt(pageIndex);
        }

        // 获取用户总数
        int totalCount = userService.getUserCount(queryUserName, queryUserRole);

        // 分页支持
        PageSupport pageSupport = new PageSupport();
        pageSupport.setPageSize(pageSize);
        pageSupport.setCurrentPageNo(currentPageNo);
        pageSupport.setTotalCount(totalCount);

        int totalPageCount = pageSupport.getTotalPageCount();

        // 控制首页和尾页行为
        if (currentPageNo < 1) {
            currentPageNo = 1;
        } else if (currentPageNo > totalPageCount) {
            currentPageNo = totalPageCount;
        }

        // 用户列表展示
        List<User> userList = userService.getUserList(queryUserName, queryUserRole, currentPageNo, pageSize);
        List<Role> roleList = roleService.getRoleList();

        model.addAttribute("userList", userList);
        model.addAttribute("roleList", roleList);
        model.addAttribute("currentPageNo", currentPageNo);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("totalPageCount", totalPageCount);
        model.addAttribute("queryUserName", queryUserName);
        model.addAttribute("queryUserRole", queryUserRole);

        return "user/list";
    }

    @GetMapping("/getrolelist")
    @ResponseBody
    public List<Role> getRoleList() {
        return roleService.getRoleList();
    }

    @GetMapping("/ucexist")
    @ResponseBody
    public HashMap<String, String> userCodeExist(@RequestParam("userCode") String userCode) {
        HashMap<String, String> resultMap = new HashMap<>();

        if (StringUtils.isNullOrEmpty(userCode)) {
            resultMap.put("userCode", "exist");
        } else {
            User user = userService.selectUserCodeExist(userCode);
            if (null != user) {
                resultMap.put("userCode", "exist");
            } else {
                resultMap.put("userCode", "notexist");
            }
        }

        return resultMap;
    }

    @GetMapping("/add")
    public String addPage() {
        return "user/add";
    }

    // 提交添加用户
    @PostMapping("/add")
    public String add(
            @RequestParam("userCode") String userCode,
            @RequestParam("userName") String userName,
            @RequestParam("userPassword") String userPassword,
            @RequestParam("gender") String gender,
            @RequestParam("birthday") String birthday,
            @RequestParam("phone") String phone,
            @RequestParam("address") String address,
            @RequestParam("userRole") String userRole,
            HttpSession session,
            Model model) {

        System.out.println("当前正在执行增加用户操作");

        User user = new User();
        user.setUserCode(userCode);
        user.setUserName(userName);
        user.setUserPassword(userPassword);
        user.setAddress(address);
        user.setGender(Integer.valueOf(gender));
        user.setPhone(phone);

        try {
            user.setBirthday(new SimpleDateFormat("yyyy-MM-dd").parse(birthday));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        user.setUserRole(Integer.valueOf(userRole));
        user.setCreationDate(new Date());

        User currentUser = (User) session.getAttribute(Constants.USER_SESSION);
        user.setCreatedBy(currentUser.getId());

        Boolean flag = userService.add(user);

        if (flag) {
            return "redirect:/user/list";
        } else {
            model.addAttribute("error", "添加用户失败");
            return "user/add";
        }
    }

    @GetMapping("/delete")
    @ResponseBody
    public HashMap<String, String> delUser(@RequestParam("uid") String id) {
        HashMap<String, String> resultMap = new HashMap<>();
        Integer delId = 0;

        try {
            delId = Integer.parseInt(id);
        } catch (Exception e) {
            delId = 0;
        }

        if (delId <= 0) {
            resultMap.put("delResult", "notexist");
        } else {
            if (userService.deleteUserById(delId)) {
                resultMap.put("delResult", "true");
            } else {
                resultMap.put("delResult", "false");
            }
        }

        return resultMap;
    }

    @GetMapping("/modify")
    public String getUserByIdModify(@RequestParam("uid") String userId, Model model) {
        if (!StringUtils.isNullOrEmpty(userId)) {
            User user = userService.getUserById(userId);
            model.addAttribute("user", user);
        }
        return "user/modify";
    }

    @GetMapping("/view")
    public String getUserByIdView(@RequestParam("uid") String userId, Model model) {
        if (!StringUtils.isNullOrEmpty(userId)) {
            User user = userService.getUserById(userId);
            model.addAttribute("user", user);
        }
        return "user/view";
    }

    @PostMapping("/modify")
    public String modify(
            @RequestParam("uid") String id,
            @RequestParam("userName") String userName,
            @RequestParam("gender") String gender,
            @RequestParam("birthday") String birthday,
            @RequestParam("phone") String phone,
            @RequestParam("address") String address,
            @RequestParam("userRole") String userRole,
            HttpSession session,
            Model model) {

        User user = new User();
        user.setId(Integer.valueOf(id));
        user.setUserName(userName);
        user.setGender(Integer.valueOf(gender));

        try {
            user.setBirthday(new SimpleDateFormat("yyyy-MM-dd").parse(birthday));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        user.setPhone(phone);
        user.setAddress(address);
        user.setUserRole(Integer.valueOf(userRole));

        User currentUser = (User) session.getAttribute(Constants.USER_SESSION);
        user.setModifyBy(currentUser.getId());
        user.setModifyDate(new Date());

        Boolean flag = userService.modify(user);

        if (flag) {
            return "redirect:/user/list";
        } else {
            model.addAttribute("error", "修改用户失败");
            return "user/modify";
        }
    }

    @GetMapping("/pwdmodify")
    public String pwdmodifyPage() {
        return "user/pwdmodify";
    }

}
