package com.viper.service.ai.tool;

import com.viper.pojo.Provider;
import com.viper.service.provider.ProviderService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ProviderQueryTool {

    private final ProviderService providerService;

    public ProviderQueryTool(ProviderService providerService) {
        this.providerService = providerService;
    }

    @Tool(name = "listProviders",
            value = "查询供应商列表（按名称/编码模糊搜索）。" +
                    "当用户问'哪些供应商''华南的供应商有哪些''供应商联系方式'时使用。" +
                    "无关键字时返回全部。")
    public List<Map<String, Object>> listProviders(
            @P("供应商名称关键字，可空") String proName,
            @P("供应商编码关键字，可空") String proCode) {

        ToolGuard.requireLogin();
        List<Provider> list = providerService.getProviderList(
                proName == null ? "" : proName,
                proCode == null ? "" : proCode);
        if (list == null) return List.of();
        return list.stream().map(p -> Map.<String, Object>of(
                "id", p.getId(),
                "proCode", p.getProCode(),
                "proName", p.getProName(),
                "contact", n(p.getProContact()),
                "phone", n(p.getProPhone()),
                "address", n(p.getUserAddress()),
                "desc", n(p.getProDesc())
        )).collect(Collectors.toList());
    }

    @Tool(name = "getProviderInfo",
            value = "根据供应商主键 id 查询供应商详细信息。")
    public Map<String, Object> getProviderInfo(@P("供应商主键 id") Long providerId) {
        ToolGuard.requireLogin();
        Provider p = providerService.getProviderById(String.valueOf(providerId));
        if (p == null) return Map.of("found", false);
        return Map.of(
                "found", true,
                "id", p.getId(),
                "proCode", p.getProCode(),
                "proName", p.getProName(),
                "contact", n(p.getProContact()),
                "phone", n(p.getProPhone()),
                "address", n(p.getUserAddress()),
                "fax", n(p.getUserFax()),
                "desc", n(p.getProDesc())
        );
    }

    private static String n(String s) { return s == null ? "" : s; }
}
