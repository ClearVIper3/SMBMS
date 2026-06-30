package com.viper.service.ai;

import com.viper.service.ai.tool.BillQueryTool;
import com.viper.service.ai.tool.InventoryTool;
import com.viper.service.ai.tool.ProviderQueryTool;
import com.viper.service.ai.tool.SalesStatisticsTool;
import com.viper.service.ai.tool.UserQueryTool;
import dev.langchain4j.agent.tool.ToolExecutor;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.agent.tool.ToolSpecifications;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 工具注册表：把所有 @Tool 注解方法收集到 (ToolSpecification, ToolExecutor) 两张索引，
 * 供 AiChatService 在调用 LLM 时一次性传给模型；执行结果也走同一份索引，避免反射错配。
 * <p>白名单语义：注册表里没有的方法，AI 永远调不到。</p>
 */
@Component
public class AiToolRegistry {

    private final List<ToolSpecification> specifications = new java.util.ArrayList<>();
    /** key = ToolSpecification.name() （即 @Tool name）*/
    private final Map<String, ToolExecutor> executors = new LinkedHashMap<>();

    public AiToolRegistry(UserQueryTool userQueryTool,
                          ProviderQueryTool providerQueryTool,
                          BillQueryTool billQueryTool,
                          InventoryTool inventoryTool,
                          SalesStatisticsTool salesStatisticsTool) {
        register(userQueryTool);
        register(providerQueryTool);
        register(billQueryTool);
        register(inventoryTool);
        register(salesStatisticsTool);
    }

    private void register(Object toolBean) {
        // 从 bean 的 class 中提取所有 @Tool 方法的规约
        List<ToolSpecification> specs = ToolSpecifications.toolSpecificationsFrom(toolBean);
        specifications.addAll(specs);
        for (Method method : toolBean.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(dev.langchain4j.agent.tool.Tool.class)) {
                String name = resolveToolName(method);
                executors.put(name, new dev.langchain4j.service.tool.DefaultToolExecutor(toolBean, method));
            }
        }
    }

    private static String resolveToolName(Method method) {
        dev.langchain4j.agent.tool.Tool ann = method.getAnnotation(dev.langchain4j.agent.tool.Tool.class);
        if (ann == null) return method.getName();
        // LangChain4j @Tool 优先取 name()；空则用 value()[0]；都空则用方法名
        if (ann.name() != null && !ann.name().isBlank()) return ann.name();
        String[] vs = ann.value();
        if (vs != null && vs.length > 0 && !vs[0].isBlank()) return method.getName();
        return method.getName();
    }

    public List<ToolSpecification> specifications() { return specifications; }

    public ToolExecutor executor(String name) { return executors.get(name); }
}
