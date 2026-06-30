package com.viper.controller;

import com.viper.controller.dto.ProviderDTO;
import com.viper.controller.dto.ProviderUpsertRequest;
import com.viper.exception.BusinessException;
import com.viper.pojo.Provider;
import com.viper.security.UserContext;
import com.viper.service.provider.ProviderService;
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
@RequestMapping("/api/providers")
public class ProviderApiController {

    private final ProviderService providerService;

    public ProviderApiController(ProviderService providerService) {
        this.providerService = providerService;
    }

    @GetMapping
    public Result list(@RequestParam(value = "proName", required = false, defaultValue = "") String proName,
                       @RequestParam(value = "proCode", required = false, defaultValue = "") String proCode) {
        List<Provider> list = providerService.getProviderList(proName, proCode);
        List<ProviderDTO> records = list == null ? List.of()
                : list.stream().map(ProviderDTO::fromEntity).collect(Collectors.toList());
        return Result.success(records);
    }

    @GetMapping("/{id}")
    public Result get(@PathVariable("id") String id) {
        Provider p = providerService.getProviderById(id);
        if (p == null) {
            throw new BusinessException("供应商不存在");
        }
        return Result.success(ProviderDTO.fromEntity(p));
    }

    @PostMapping
    public Result add(@RequestBody ProviderUpsertRequest req) {
        UserContext.CurrentUser cu = UserContext.require();

        Provider p = new Provider();
        p.setProCode(req.getProCode());
        p.setProName(req.getProName());
        p.setProDesc(req.getProDesc());
        p.setProContact(req.getProContact());
        p.setProPhone(req.getProPhone());
        p.setUserAddress(req.getUserAddress());
        p.setUserFax(req.getUserFax());
        p.setCreatedBy(cu.getId());
        p.setCreationDate(new Date());

        boolean ok = providerService.add(p);
        return ok ? Result.success(ProviderDTO.fromEntity(p)) : Result.error("添加供应商失败");
    }

    @PutMapping("/{id}")
    public Result modify(@PathVariable("id") Long id, @RequestBody ProviderUpsertRequest req) {
        UserContext.CurrentUser cu = UserContext.require();

        Provider p = new Provider();
        p.setId(id);
        p.setProName(req.getProName());
        p.setProDesc(req.getProDesc());
        p.setProContact(req.getProContact());
        p.setProPhone(req.getProPhone());
        p.setUserAddress(req.getUserAddress());
        p.setUserFax(req.getUserFax());
        p.setModifyBy(cu.getId());
        p.setModifyDate(new Date());

        boolean ok = providerService.modify(p);
        return ok ? Result.success("修改成功") : Result.error("修改失败");
    }

    /**
     * 删除规则与旧 ProviderService 行为对齐：
     *   - 返回 0 表示删除成功；
     *   - 返回 >0 表示存在订单引用、被拒绝（值=订单数）；
     *   - 返回 -1 表示异常。
     * 这里把 >0 翻译成 409 业务异常，前端拿到更直观的语义。
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable("id") String id) {
        int r = providerService.deleteProviderById(id);
        if (r == 0)  return Result.success("删除成功");
        if (r == -1) return Result.error("删除失败");
        throw new BusinessException("该供应商下还有 " + r + " 条订单，不可删除");
    }
}
