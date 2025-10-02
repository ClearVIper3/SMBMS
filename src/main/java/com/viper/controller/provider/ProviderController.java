package com.viper.controller.provider;

import com.alibaba.fastjson2.JSONArray;
import com.mysql.cj.util.StringUtils;
import com.viper.pojo.Provider;
import com.viper.pojo.User;
import com.viper.service.provider.ProviderService;
import com.viper.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Controller
@RequestMapping("/jsp/provider.do")
public class ProviderController{

    private final ProviderService providerService;

    @Autowired
    public ProviderController(ProviderService providerService) {
        this.providerService = providerService;
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST})
    public void handleRequest(
            @RequestParam("method") String method,
            HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException{

        if(method != null && method.equals("query")){
            this.query(request, response);
        } else if(method != null && method.equals("delprovider")){
            this.delProvider(request, response);
        } else if(method != null && method.equals("add")){
            this.add(request, response);
        } else if(method != null && method.equals("view")){
            this.getProviderById(request, response,"providerview.jsp");
        } else if(method != null && method.equals("modify")){
            this.getProviderById(request, response,"providermodify.jsp");
        }else if(method != null && method.equals("modifysave")){
            this.modify(request, response);
        }
    }

    private void query(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String queryProName = request.getParameter("queryProName");
        String queryProCode = request.getParameter("queryProCode");
        if(StringUtils.isNullOrEmpty(queryProName)){
            queryProName = "";
        }
        if(StringUtils.isNullOrEmpty(queryProCode)){
            queryProCode = "";
        }
        List<Provider> providerList = new ArrayList<Provider>();
        providerList = providerService.getProviderList(queryProName,queryProCode);
        request.setAttribute("providerList", providerList);
        request.setAttribute("queryProName", queryProName);
        request.setAttribute("queryProCode", queryProCode);
        request.getRequestDispatcher("providerlist.jsp").forward(request, response);
    }

    private void delProvider(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String delId = request.getParameter("proid");
        HashMap<String,String> resultMap = new HashMap<String,String>();
        if(!StringUtils.isNullOrEmpty(delId)){
            int flag = providerService.deleteProviderById(delId);
            if(flag == 0){ //删除成功
                resultMap.put("delResult", "true");
            } else if(flag == -1) { // 删除失败
                resultMap.put("delResult", "false");
            } else if(flag > 0){ // 该供应商下有订单，不可删除，返回订单数
                resultMap.put("delResult", String.valueOf(flag));
            }
        } else {
            resultMap.put("delResult", "notexist");
        }
        //把resultMap转化成json对象输出
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.print(JSONArray.toJSONString(resultMap));
        out.flush();
        out.close();
    }

    private void add(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String proCode = request.getParameter("proCode");
        String proName = request.getParameter("proName");
        String proContact = request.getParameter("proContact");
        String proPhone = request.getParameter("proPhone");
        String proAddress = request.getParameter("userAddress");
        String proFax = request.getParameter("proFax");
        String proDesc = request.getParameter("proDesc");

        Provider provider = new Provider();
        provider.setProCode(proCode);
        provider.setProName(proName);
        provider.setProContact(proContact);
        provider.setProPhone(proPhone);
        provider.setUserFax(proFax);
        provider.setUserAddress(proAddress);
        provider.setProDesc(proDesc);
        provider.setCreatedBy(((User)request.getSession().getAttribute(Constants.USER_SESSION)).getId());
        provider.setCreationDate(new Date());

        boolean flag;
        flag = providerService.add(provider);
        if(flag){
            response.sendRedirect(request.getContextPath()+"/jsp/provider.do?method=query");
        } else{
            request.getRequestDispatcher("provideradd.jsp").forward(request, response);
        }
    }

    private void getProviderById(HttpServletRequest request, HttpServletResponse response,String url) throws ServletException, IOException {
        String id = request.getParameter("proid");
        if(!StringUtils.isNullOrEmpty(id)){
            Provider provider = providerService.getProviderById(id);
            request.setAttribute("provider", provider);
            request.getRequestDispatcher(url).forward(request, response);
        }
    }

    private void modify(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String proName = request.getParameter("proName");
        String proContact = request.getParameter("proContact");
        String proPhone = request.getParameter("proPhone");
        String proAddress = request.getParameter("userAddress");
        String proFax = request.getParameter("userFax");
        String proDesc = request.getParameter("proDesc");
        String id = request.getParameter("id");
        Provider provider = new Provider();
        provider.setProName(proName);
        provider.setId(Integer.valueOf(id));
        provider.setProContact(proContact);
        provider.setProPhone(proPhone);
        provider.setUserFax(proFax);
        provider.setUserAddress(proAddress);
        provider.setProDesc(proDesc);
        provider.setModifyBy(((User)request.getSession().getAttribute(Constants.USER_SESSION)).getId());
        provider.setModifyDate(new Date());
        boolean flag = false;
        flag = providerService.modify(provider);
        if(flag){
            response.sendRedirect(request.getContextPath()+"/jsp/provider.do?method=query");
        }else{
            request.getRequestDispatcher("providermodify.jsp").forward(request, response);
        }
    }
}
