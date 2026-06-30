package com.viper.controller;

import com.viper.controller.dto.BillDTO;
import com.viper.controller.dto.BillUpsertRequest;
import com.viper.exception.BusinessException;
import com.viper.pojo.Bill;
import com.viper.security.UserContext;
import com.viper.service.bill.BillService;
import com.viper.utils.Result;
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

@RestController
@RequestMapping("/api/bills")
public class BillApiController {

    private final BillService billService;

    public BillApiController(BillService billService) {
        this.billService = billService;
    }

    @GetMapping
    public Result list(@RequestParam(value = "productName", required = false, defaultValue = "") String productName,
                       @RequestParam(value = "providerId", required = false, defaultValue = "0") Long providerId,
                       @RequestParam(value = "isPayment", required = false, defaultValue = "0") Integer isPayment) {
        Bill query = new Bill();
        query.setProductName(productName);
        query.setProviderId(providerId);
        query.setIsPayment(isPayment);
        List<Bill> bills = billService.getBillList(query);
        List<BillDTO> records = bills == null ? List.of()
                : bills.stream().map(BillDTO::fromEntity).collect(Collectors.toList());
        return Result.success(records);
    }

    @GetMapping("/{id}")
    public Result get(@PathVariable("id") String id) {
        Bill b = billService.getBillById(id);
        if (b == null) {
            throw new BusinessException("订单不存在");
        }
        return Result.success(BillDTO.fromEntity(b));
    }

    @PostMapping
    public Result add(@RequestBody BillUpsertRequest req) {
        UserContext.CurrentUser cu = UserContext.require();

        Bill b = new Bill();
        b.setBillCode(req.getBillCode());
        b.setProductName(req.getProductName());
        b.setProductDesc(req.getProductDesc());
        b.setProductUnit(req.getProductUnit());
        b.setProductCount(req.getProductCount());
        b.setTotalPrice(req.getTotalPrice());
        b.setIsPayment(req.getIsPayment());
        b.setProviderId(req.getProviderId());
        b.setCreatedBy(cu.getId());
        b.setCreationDate(new Date());

        boolean ok = billService.add(b);
        return ok ? Result.success(BillDTO.fromEntity(b)) : Result.error("添加订单失败");
    }

    @PutMapping("/{id}")
    public Result modify(@PathVariable("id") Long id, @RequestBody BillUpsertRequest req) {
        UserContext.CurrentUser cu = UserContext.require();

        Bill b = new Bill();
        b.setId(id);
        b.setProductName(req.getProductName());
        b.setProductDesc(req.getProductDesc());
        b.setProductUnit(req.getProductUnit());
        b.setProductCount(req.getProductCount());
        b.setTotalPrice(req.getTotalPrice());
        b.setIsPayment(req.getIsPayment());
        b.setProviderId(req.getProviderId());
        b.setModifyBy(cu.getId());
        b.setModifyDate(new Date());

        boolean ok = billService.modify(b);
        return ok ? Result.success("修改成功") : Result.error("修改失败");
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable("id") String id) {
        return billService.deleteBillById(id) ? Result.success("删除成功") : Result.error("删除失败");
    }
}
