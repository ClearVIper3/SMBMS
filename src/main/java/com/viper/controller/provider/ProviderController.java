package com.viper.controller.provider;

import com.mysql.cj.util.StringUtils;
import com.viper.pojo.Provider;
import com.viper.pojo.User;
import com.viper.service.provider.ProviderService;
import com.viper.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
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

    @RequestMapping(params = "method=query")
    public String query(
            @RequestParam(value = "queryProName", required = false) String queryProName,
            @RequestParam(value = "queryProCode", required = false) String queryProCode,
            Model model) {

        // 处理空值
        if (StringUtils.isNullOrEmpty(queryProName)) {
            queryProName = "";
        }
        if (StringUtils.isNullOrEmpty(queryProCode)) {
            queryProCode = "";
        }

        List<Provider> providerList = providerService.getProviderList(queryProName, queryProCode);
        model.addAttribute("providerList", providerList);
        model.addAttribute("queryProName", queryProName);
        model.addAttribute("queryProCode", queryProCode);

        return "providerlist";
    }

    @RequestMapping(params = "method=delprovider")
    @ResponseBody
    public HashMap<String, String> delProvider(@RequestParam("proid") String proId) {
        HashMap<String, String> resultMap = new HashMap<>();
        if (!StringUtils.isNullOrEmpty(proId)) {
            int flag = providerService.deleteProviderById(proId);
            if (flag == 0) { // 删除成功
                resultMap.put("delResult", "true");
            } else if (flag == -1) { // 删除失败
                resultMap.put("delResult", "false");
            } else if (flag > 0) { // 该供应商下有订单，不可删除，返回订单数
                resultMap.put("delResult", String.valueOf(flag));
            }
        } else {
            resultMap.put("delResult", "notexist");
        }
        return resultMap;
    }

    @RequestMapping(params = "method=add", method = RequestMethod.GET)
    public String addPage() {
        return "provideradd";
    }

    @RequestMapping(params = "method=add", method = RequestMethod.POST)
    public String add(
            @RequestParam("proCode") String proCode,
            @RequestParam("proName") String proName,
            @RequestParam("proContact") String proContact,
            @RequestParam("proPhone") String proPhone,
            @RequestParam("userAddress") String userAddress,
            @RequestParam("proFax") String proFax,
            @RequestParam("proDesc") String proDesc,
            HttpSession session) {

        Provider provider = new Provider();
        provider.setProCode(proCode);
        provider.setProName(proName);
        provider.setProContact(proContact);
        provider.setProPhone(proPhone);
        provider.setUserFax(proFax);
        provider.setUserAddress(userAddress);
        provider.setProDesc(proDesc);

        User user = (User) session.getAttribute(Constants.USER_SESSION);
        provider.setCreatedBy(user.getId());
        provider.setCreationDate(new Date());

        boolean flag = providerService.add(provider);
        if (flag) {
            return "redirect:http://localhost:8080/smbms/jsp/provider.do?method=query";
        } else {
            return "provideradd";
        }
    }

    @RequestMapping(params = "method=view")
    public String getProviderById(@RequestParam("proid") String proId, Model model) {
        if (!StringUtils.isNullOrEmpty(proId)) {
            Provider provider = providerService.getProviderById(proId);
            model.addAttribute("provider", provider);
        }
        return "providerview";
    }

    @RequestMapping(params = "method=modify")
    public String getProviderByIdModify(@RequestParam("proid") String proId, Model model) {
        if (!StringUtils.isNullOrEmpty(proId)) {
            Provider provider = providerService.getProviderById(proId);
            model.addAttribute("provider", provider);
        }
        return "providermodify";
    }

    @RequestMapping(params = "method=modifysave", method = RequestMethod.POST)
    public String modify(
            @RequestParam("id") Integer id,
            @RequestParam("proName") String proName,
            @RequestParam("proContact") String proContact,
            @RequestParam("proPhone") String proPhone,
            @RequestParam("userAddress") String userAddress,
            @RequestParam("userFax") String userFax,
            @RequestParam("proDesc") String proDesc,
            HttpSession session) {

        Provider provider = new Provider();
        provider.setId(id);
        provider.setProName(proName);
        provider.setProContact(proContact);
        provider.setProPhone(proPhone);
        provider.setUserFax(userFax);
        provider.setUserAddress(userAddress);
        provider.setProDesc(proDesc);

        User user = (User) session.getAttribute(Constants.USER_SESSION);
        provider.setModifyBy(user.getId());
        provider.setModifyDate(new Date());

        boolean flag = providerService.modify(provider);
        if (flag) {
            return "redirect:/jsp/provider.do?method=query";
        } else {
            return "providermodify";
        }
    }
}
