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

import java.util.List;

@RestController
@RequestMapping("/api/providers")
public class ProviderApiController {

    private final ProviderService providerService;

    public ProviderApiController(ProviderService providerService) {
        this.providerService = providerService;
    }

    @GetMapping
    public Result<List<ProviderDTO>> list(
            @RequestParam(value = "proName", required = false, defaultValue = "") String proName,
            @RequestParam(value = "proCode", required = false, defaultValue = "") String proCode) {

        List<Provider> list = providerService.getProviderList(proName, proCode);
        List<ProviderDTO> records = list == null ? List.of() : list.stream().map(ProviderDTO::fromEntity).toList();
        return Result.success(records);
    }

    @GetMapping("/{id}")
    public Result<ProviderDTO> get(@PathVariable("id") String id) {
        Provider p = providerService.getProviderById(id);
        if (p == null) throw new BusinessException("供应商不存在");
        return Result.success(ProviderDTO.fromEntity(p));
    }

    @PostMapping
    public Result<ProviderDTO> add(@RequestBody ProviderUpsertRequest req) {
        Provider p = req.toNewEntity(UserContext.require().getId());
        if (!providerService.add(p)) throw new BusinessException("添加供应商失败");
        return Result.success(ProviderDTO.fromEntity(p));
    }

    @PutMapping("/{id}")
    public Result<String> modify(@PathVariable("id") Long id, @RequestBody ProviderUpsertRequest req) {
        Provider p = req.toUpdateEntity(id, UserContext.require().getId());
        if (!providerService.modify(p)) throw new BusinessException("修改供应商失败");
        return Result.successMsg("修改成功");
    }

    /**
     * 翻译 ProviderService 的三态返回值：
     *   0  → 删除成功
     *   -1 → 兜底错误
     *   >0 → 该供应商下还有 N 条订单，业务异常
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable("id") String id) {
        int r = providerService.deleteProviderById(id);
        if (r == 0)  return Result.successMsg("删除成功");
        if (r == -1) throw new BusinessException("删除失败");
        throw new BusinessException("该供应商下还有 " + r + " 条订单，不可删除");
    }
}
