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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Controller
@RequestMapping("/bill")
public class BillController{

    private final BillService billService;
    private final ProviderService providerService;

    // 构造器注入
    public BillController(BillService billService, ProviderService providerService) {
        this.billService = billService;
        this.providerService = providerService;
    }

    @GetMapping("/list")
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
            bill.setProviderId(0L);
        } else {
            bill.setProviderId(Long.parseLong(queryProviderId));
        }
        bill.setProductName(queryProductName);

        List<Bill> billList = billService.getBillList(bill);
        model.addAttribute("billList", billList);
        model.addAttribute("queryProductName", queryProductName);
        model.addAttribute("queryProviderId", queryProviderId);
        model.addAttribute("queryIsPayment", queryIsPayment);

        return "bill/list";
    }

    @GetMapping("/view")
    public String getBillById(@RequestParam("billid") String billId, Model model) {
        if (!StringUtils.isNullOrEmpty(billId)) {
            Bill bill = billService.getBillById(billId);
            model.addAttribute("bill", bill);
        }
        return "bill/view";
    }

    @GetMapping("/modify")
    public String getBillByIdModify(@RequestParam("billid") String billId, Model model) {
        if (!StringUtils.isNullOrEmpty(billId)) {
            Bill bill = billService.getBillById(billId);
            model.addAttribute("bill", bill);
        }
        return "bill/modify";
    }

    @PostMapping("/modify")
    public String modify(
            @RequestParam("id") Long id,
            @RequestParam("productName") String productName,
            @RequestParam(value = "productDesc", required = false) String productDesc,
            @RequestParam("productUnit") String productUnit,
            @RequestParam("productCount") String productCount,
            @RequestParam("totalPrice") String totalPrice,
            @RequestParam("providerId") Long providerId,
            @RequestParam("isPayment") Integer isPayment,
            HttpSession session) {

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
            return "redirect:/bill/list";
        } else {
            return "bill/modify";
        }
    }

    @GetMapping("/delete")
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

    @GetMapping("/add")
    public String addPage() {
        return "bill/add";
    }

    @PostMapping("/add")
    public String add(
            @RequestParam("billCode") String billCode,
            @RequestParam("productName") String productName,
            @RequestParam(value = "productDesc", required = false) String productDesc,
            @RequestParam("productUnit") String productUnit,
            @RequestParam("productCount") String productCount,
            @RequestParam("totalPrice") String totalPrice,
            @RequestParam("providerId") Long providerId,
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

        if (flag) {
            return "redirect:/bill/list";
        } else {
            return "bill/add";
        }
    }

    @GetMapping("/getproviderlist")
    @ResponseBody
    public List<Provider> getProviderlist() {
        return providerService.getProviderList("", "");
    }
}
