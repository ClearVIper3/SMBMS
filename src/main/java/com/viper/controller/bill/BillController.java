package com.viper.controller.bill;

import com.viper.pojo.Bill;
import com.viper.pojo.Provider;
import com.viper.pojo.User;
import com.viper.service.bill.BillService;
import com.viper.service.provider.ProviderService;
import com.viper.utils.Constants;
import com.mysql.cj.util.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Controller
@RequestMapping("/jsp/bill.do")
public class BillController{

    private final BillService billService;
    private final ProviderService providerService;

    // 构造器注入
    public BillController(BillService billService, ProviderService providerService) {
        this.billService = billService;
        this.providerService = providerService;
    }

    @RequestMapping(params = "method=query")
    public String query(
            @RequestParam(value = "queryProductName", required = false) String queryProductName,
            @RequestParam(value = "queryProviderId", required = false) String queryProviderId,
            @RequestParam(value = "queryIsPayment", required = false) String queryIsPayment,
            Model model){

        List<Provider> providerList = providerService.getProviderList("", "");
        model.addAttribute("providerList", providerList);

        if (StringUtils.isNullOrEmpty(queryProductName)) {
            queryProductName = "";
        }

        Bill bill = new Bill();
        if (StringUtils.isNullOrEmpty(queryIsPayment)) {
            bill.setIsPayment(0);
        } else {
            bill.setIsPayment(Integer.parseInt(queryIsPayment));
        }
        if (StringUtils.isNullOrEmpty(queryProviderId)) {
            bill.setProviderId(0);
        } else {
            bill.setProviderId(Integer.parseInt(queryProviderId));
        }
        bill.setProductName(queryProductName);

        List<Bill> billList = billService.getBillList(bill);
        model.addAttribute("billList", billList);
        model.addAttribute("queryProductName", queryProductName);
        model.addAttribute("queryProviderId", queryProviderId);
        model.addAttribute("queryIsPayment", queryIsPayment);

        return "billlist";
    }

    @RequestMapping(params = "method=view")
    public String getBillById(@RequestParam("billid") String billId, Model model) {
        if (!StringUtils.isNullOrEmpty(billId)) {
            Bill bill = billService.getBillById(billId);
            model.addAttribute("bill", bill);
        }
        return "billview";
    }

    @RequestMapping(params = "method=modify")
    public String getBillByIdModify(@RequestParam("billid") String billId, Model model) {
        if (!StringUtils.isNullOrEmpty(billId)) {
            Bill bill = billService.getBillById(billId);
            model.addAttribute("bill", bill);
        }
        return "billmodify";
    }

    @RequestMapping(params = "method=modifysave", method = RequestMethod.POST)
    public String modify(
            @RequestParam("id") Integer id,
            @RequestParam("productName") String productName,
            @RequestParam(value = "productDesc", required = false) String productDesc,
            @RequestParam("productUnit") String productUnit,
            @RequestParam("productCount") String productCount,
            @RequestParam("totalPrice") String totalPrice,
            @RequestParam("providerId") Integer providerId,
            @RequestParam("isPayment") Integer isPayment,
            HttpSession session) {

        System.out.println("modify===============");

        Bill bill = new Bill();
        bill.setId(id);
        bill.setProductName(productName);
        bill.setProductDesc(productDesc);
        bill.setProductUnit(productUnit);
        bill.setProductCount(new BigDecimal(productCount).setScale(2, BigDecimal.ROUND_DOWN));
        bill.setIsPayment(isPayment);
        bill.setTotalPrice(new BigDecimal(totalPrice).setScale(2, BigDecimal.ROUND_DOWN));
        bill.setProviderId(providerId);

        User user = (User) session.getAttribute(Constants.USER_SESSION);
        bill.setModifyBy(user.getId());
        bill.setModifyDate(new Date());

        boolean flag = billService.modify(bill);
        if (flag) {
            return "redirect:/jsp/bill.do?method=query";
        } else {
            return "billmodify";
        }
    }

    @RequestMapping(params = "method=delbill")
    @ResponseBody
    public HashMap<String, String> delBill(@RequestParam("billid") String billId) {
        HashMap<String, String> resultMap = new HashMap<>();
        if (!StringUtils.isNullOrEmpty(billId)) {
            boolean flag = billService.deleteBillById(billId);
            if (flag) {
                resultMap.put("delResult", "true");
            } else {
                resultMap.put("delResult", "false");
            }
        } else {
            resultMap.put("delResult", "notexit");
        }
        return resultMap;
    }

    @RequestMapping(params = "method=add", method = RequestMethod.POST)
    public String add(
            @RequestParam("billCode") String billCode,
            @RequestParam("productName") String productName,
            @RequestParam(value = "productDesc", required = false) String productDesc,
            @RequestParam("productUnit") String productUnit,
            @RequestParam("productCount") String productCount,
            @RequestParam("totalPrice") String totalPrice,
            @RequestParam("providerId") Integer providerId,
            @RequestParam("isPayment") Integer isPayment,
            HttpSession session) {

        Bill bill = new Bill();
        bill.setBillCode(billCode);
        bill.setProductName(productName);
        bill.setProductDesc(productDesc);
        bill.setProductUnit(productUnit);
        bill.setProductCount(new BigDecimal(productCount).setScale(2, BigDecimal.ROUND_DOWN));
        bill.setIsPayment(isPayment);
        bill.setTotalPrice(new BigDecimal(totalPrice).setScale(2, BigDecimal.ROUND_DOWN));
        bill.setProviderId(providerId);

        User user = (User) session.getAttribute(Constants.USER_SESSION);
        bill.setCreatedBy(user.getId());
        bill.setCreationDate(new Date());

        boolean flag = billService.add(bill);
        System.out.println("add flag -- > " + flag);

        if (flag) {
            return "redirect:/jsp/bill.do?method=query";
        } else {
            return "billadd";
        }
    }

    @RequestMapping(params = "method=getproviderlist")
    @ResponseBody
    public List<Provider> getProviderlist() {
        System.out.println("getproviderlist ========================= ");
        return providerService.getProviderList("", "");
    }
}