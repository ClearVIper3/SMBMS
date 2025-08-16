package com.viper.servlet.user;

import com.alibaba.fastjson2.JSONArray;
import com.mysql.cj.util.StringUtils;
import com.mysql.cj.xdevapi.JsonArray;
import com.viper.pojo.Role;
import com.viper.pojo.User;
import com.viper.service.role.RoleService;
import com.viper.service.role.RoleServiceImpl;
import com.viper.service.user.UserService;
import com.viper.service.user.UserServiceImpl;
import com.viper.utils.Constants;
import com.viper.utils.PageSupport;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

//实现Servlet复用
public class UserServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String method = req.getParameter("method");
        if (method != null && method.equals("savepwd")) {
            this.updateUser(req, resp);
        } else if (method != null && method.equals("pwdmodify")) {
            this.pwdModify(req, resp);
        } else if (method != null && method.equals("query")) {
            this.query(req, resp);
        } else if(method != null && method.equals("ucexist")) {
            this.userCodeExist(req, resp);
        } else if(method != null && method.equals("getrolelist")) {
            this.getRoleList(req, resp);
        } else if(method != null && method.equals("add")) {
            this.add(req,resp);
        } else if(method != null && method.equals("deluser")){
            this.delUser(req,resp);
        } else if(method != null && method.equals("modify")){
            //通过用户id得到用户
            this.getUserById(req, resp,"usermodify.jsp");
        } else if(method != null && method.equals("modifyexe")){
            this.modify(req, resp);
        } else if(method != null && method.equals("view")){
            this.getUserById(req, resp,"userview.jsp");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

    public void updateUser(HttpServletRequest req, HttpServletResponse resp){
        Object attribute = req.getSession().getAttribute(Constants.USER_SESSION);

        //System.out.println("来了来了label");

        String password = req.getParameter("newpassword");

        Boolean flag = false;

        //System.out.println(attribute != null);
        //System.out.println(!StringUtils.isNullOrEmpty(password));

        if(attribute != null && !StringUtils.isNullOrEmpty(password)) {
            UserService userService = new UserServiceImpl();
            flag = userService.PasswordModify(((User)attribute).getId(), password);

            if(flag) {
                req.setAttribute("message","修改密码成功，请退出，使用新密码登录");
                req.getSession().removeAttribute(Constants.USER_SESSION);
                //移除Session -> filter
            } else{
                req.setAttribute("message","修改密码失败");
            }
        } else {
            req.setAttribute("message","新密码有问题");
        }

        try {
            req.getRequestDispatcher("pwdmodify.jsp").forward(req, resp);
        } catch (ServletException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void pwdModify(HttpServletRequest req, HttpServletResponse resp){

        Object o = req.getSession().getAttribute(Constants.USER_SESSION);
        String oldPassword = req.getParameter("oldpassword");

        HashMap<String,String> resultMap = new HashMap<>();

        if(o == null){
            resultMap.put("result","sessionerror");
        }else if(StringUtils.isNullOrEmpty(oldPassword)) {
            resultMap.put("result","error");
        }else{
            String userPassword = ((User)o).getUserPassword();
            if(userPassword.equals(oldPassword)) {
                resultMap.put("result","true");
            }else{
                resultMap.put("result","false");
            }
        }

        try{
            resp.setContentType("application/json");
            PrintWriter writer = resp.getWriter();
            /*
                resultMap = ["result", "sessionerror"]
                Json格式 = {key : value}
             */
            writer.write(JSONArray.toJSONString(resultMap));
            writer.flush();
            writer.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void query(HttpServletRequest req, HttpServletResponse resp){

        //查询用户列表

        //从前端获取数据
        String queryUserName = req.getParameter("queryname"); //查询人名
        String temp = req.getParameter("queryUserRole");  //查询职位
        String pageIndex = req.getParameter("pageIndex"); //当前页数
        int queryUserRole = 0;

        //获取用户列表
        UserService userService = new UserServiceImpl();

        //第一次走这个请求，一定是第一页，页面大小固定
        int pageSize = 5; //可优化为CONSTANT
        int currentPageNo = 1;

        if(queryUserName == null){
            queryUserName = "";
        }
        if(temp != null && !temp.equals("")){
            queryUserRole = Integer.parseInt(temp); //给查询赋值0，1，2，3
        }
        if(pageIndex != null){
            currentPageNo = Integer.parseInt(pageIndex);
        }

        //获取用户的总数（分页：上一页，下一页的情况）
        int totalCount = userService.getUserCount(queryUserName, queryUserRole);
        //总页数支持
        PageSupport pageSupport = new PageSupport();
        pageSupport.setPageSize(pageSize);
        pageSupport.setCurrentPageNo(currentPageNo);
        pageSupport.setTotalCount(totalCount);

        //控制首页和尾页行为
        int totalPageCount = pageSupport.getTotalPageCount();

        if(currentPageNo < 1){
            currentPageNo = 1;
        } else if(currentPageNo > totalPageCount){
            currentPageNo = totalPageCount;
        }

        //用户列表展示
        List<User> userList = userService.getUserList(queryUserName, queryUserRole, currentPageNo, pageSize);
        req.setAttribute("userList", userList);

        RoleService roleService = new RoleServiceImpl();
        List<Role> roleList = roleService.getRoleList();
        req.setAttribute("roleList", roleList);
        req.setAttribute("currentPageNo", currentPageNo);
        req.setAttribute("totalCount", totalCount);
        req.setAttribute("totalPageCount", totalPageCount);
        req.setAttribute("queryUserName", queryUserName);
        req.setAttribute("queryUserRole", queryUserRole);

        try{
            req.getRequestDispatcher("userlist.jsp").forward(req, resp);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    private void getRoleList(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
        List<Role> roleList = null;
        RoleService roleService = new RoleServiceImpl();
        roleList = roleService.getRoleList();
        //把roleList转换成json对象输出
        resp.setContentType("application/json");
        PrintWriter outPrintWriter = resp.getWriter();
        outPrintWriter.write(JSONArray.toJSONString(roleList));
        outPrintWriter.flush();
        outPrintWriter.close();
    }

    private void userCodeExist(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
        //先拿到用户的编码
        String userCode = req.getParameter("userCode");
        //用一个hashmap，暂存现在所有现存的用户编码
        HashMap<String, String> resultMap = new HashMap<String, String>();
        if(StringUtils.isNullOrEmpty(userCode)){
            //userCode == null || userCode.equals("")
            //如果输入的这个编码为空或者不存在，说明可用
            resultMap.put("userCode", "exist");
        }else{//如果输入的编码不为空，则需要去找一下是否存在这个用户
            UserService userService = new UserServiceImpl();
            User user = userService.selectUserCodeExist(userCode);
            if(null != user){
                resultMap.put("userCode","exist");
            }else{
                resultMap.put("userCode", "notexist");
            }
        }
        //把resultMap转为json字符串以json的形式输出
        //配置上下文的输出类型
        resp.setContentType("application/json");
        //从response对象中获取往外输出的writer对象
        PrintWriter outPrintWriter = resp.getWriter();
        //把resultMap转为json字符串 输出
        outPrintWriter.write(JSONArray.toJSONString(resultMap));
        outPrintWriter.flush();//刷新
        outPrintWriter.close();//关闭流
    }

    private void add(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
        System.out.println("当前正在执行增加用户操作");
        //从前端得到页面的请求的参数即用户输入的值
        String userCode = req.getParameter("userCode");
        String userName = req.getParameter("userName");
        String userPassword = req.getParameter("userPassword");
        //String ruserPassword = req.getParameter("ruserPassword");
        String gender = req.getParameter("gender");
        String birthday = req.getParameter("birthday");
        String phone = req.getParameter("phone");
        String address = req.getParameter("address");
        String userRole = req.getParameter("userRole");
        //把这些值塞进一个用户属性中
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
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        user.setUserRole(Integer.valueOf(userRole));
        user.setCreationDate(new Date());
        //查找当前正在登陆的用户的id
        user.setCreatedBy(((User)req.getSession().getAttribute(Constants.USER_SESSION)).getId());
        UserServiceImpl userService = new UserServiceImpl();
        Boolean flag = userService.add(user);
        //如果添加成功，则页面转发，否则重新刷新，再次跳转到当前页面
        if(flag){
            resp.sendRedirect(req.getContextPath()+"/jsp/user.do?method=query");
        }else{
            req.getRequestDispatcher("useradd.jsp").forward(req,resp);
        }
    }

    private void delUser(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
        String id = req.getParameter("uid");
        Integer delId = 0;
        try{
            delId = Integer.parseInt(id);
        }catch (Exception e) {
            // TODO: handle exception
            delId = 0;
        }
        //需要判断是否能删除成功
        HashMap<String, String> resultMap = new HashMap<String, String>();
        if(delId <= 0){
            resultMap.put("delResult", "notexist");
        }else{
            UserService userService = new UserServiceImpl();
            if(userService.deleteUserById(delId)){
                resultMap.put("delResult", "true");
            }else{
                resultMap.put("delResult", "false");
            }
        }

        //把resultMap转换成json对象输出
        resp.setContentType("application/json");
        PrintWriter outPrintWriter = resp.getWriter();
        outPrintWriter.write(JSONArray.toJSONString(resultMap));
        outPrintWriter.flush();
        outPrintWriter.close();
    }

    private void getUserById(HttpServletRequest req, HttpServletResponse resp,String url) throws ServletException, IOException{
        String id = req.getParameter("uid");
        if(!StringUtils.isNullOrEmpty(id)){
            //调用后台方法得到user对象
            UserService userService = new UserServiceImpl();
            User user = userService.getUserById(id);
            req.setAttribute("user", user);
            req.getRequestDispatcher(url).forward(req, resp);
        }
    }


    //修改用户信息
    private void modify(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
        //需要拿到前端传递进来的参数
        String id = req.getParameter("uid");;
        String userName = req.getParameter("userName");
        String gender = req.getParameter("gender");
        String birthday = req.getParameter("birthday");
        String phone = req.getParameter("phone");
        String address = req.getParameter("address");
        String userRole = req.getParameter("userRole");

        //创建一个user对象接收这些参数
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
        user.setModifyBy(((User)req.getSession().getAttribute(Constants.USER_SESSION)).getId());
        user.setModifyDate(new Date());

        //调用service层
        UserServiceImpl userService = new UserServiceImpl();
        Boolean flag = userService.modify(user);

        //判断是否修改成功来决定跳转到哪个页面
        if(flag){
            resp.sendRedirect(req.getContextPath()+"/jsp/user.do?method=query");
        }else{
            req.getRequestDispatcher("usermodify.jsp").forward(req, resp);
        }

    }
}
