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

import java.util.List;

@RestController
@RequestMapping("/api/bills")
public class BillApiController {

    private final BillService billService;

    public BillApiController(BillService billService) {
        this.billService = billService;
    }

    @GetMapping
    public Result<List<BillDTO>> list(
            @RequestParam(value = "productName", required = false, defaultValue = "") String productName,
            @RequestParam(value = "providerId", required = false, defaultValue = "0") Long providerId,
            @RequestParam(value = "isPayment", required = false, defaultValue = "0") Integer isPayment) {

        Bill query = new Bill();
        query.setProductName(productName);
        query.setProviderId(providerId);
        query.setIsPayment(isPayment);
        List<Bill> bills = billService.getBillList(query);
        List<BillDTO> records = bills == null ? List.of() : bills.stream().map(BillDTO::fromEntity).toList();
        return Result.success(records);
    }

    @GetMapping("/{id}")
    public Result<BillDTO> get(@PathVariable("id") String id) {
        Bill b = billService.getBillById(id);
        if (b == null) throw new BusinessException("订单不存在");
        return Result.success(BillDTO.fromEntity(b));
    }

    @PostMapping
    public Result<BillDTO> add(@RequestBody BillUpsertRequest req) {
        Bill b = req.toNewEntity(UserContext.require().getId());
        if (!billService.add(b)) throw new BusinessException("添加订单失败");
        return Result.success(BillDTO.fromEntity(b));
    }

    @PutMapping("/{id}")
    public Result<String> modify(@PathVariable("id") Long id, @RequestBody BillUpsertRequest req) {
        Bill b = req.toUpdateEntity(id, UserContext.require().getId());
        if (!billService.modify(b)) throw new BusinessException("修改订单失败");
        return Result.successMsg("修改成功");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable("id") String id) {
        if (!billService.deleteBillById(id)) throw new BusinessException("删除失败");
        return Result.successMsg("删除成功");
    }
}
