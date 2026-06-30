package com.viper.service.ai.tool;

import com.viper.pojo.Bill;
import com.viper.service.bill.BillService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 库存预警工具。
 * <p>本系统没有独立的库存表，按业务语义近似实现：
 * 把 smbms_bill 中相同 productName 的 productCount 累加，视为"在手库存近似值"，
 * 累加值低于阈值的商品即为"库存不足"。
 * 工具描述里讲清口径，避免 AI/用户误解。</p>
 */
@Component
public class InventoryTool {

    private final BillService billService;

    public InventoryTool(BillService billService) {
        this.billService = billService;
    }

    @Tool(name = "getLowStockProducts",
            value = "查询库存不足的商品。口径：当前系统没有独立库存表，本工具以 smbms_bill 中按商品名聚合的进货总数(productCount)" +
                    " 近似估计库存，低于阈值的视为不足。返回按库存升序的商品列表。" +
                    "当用户问'库存不足的商品''哪些商品需要补货'时使用。")
    public List<Map<String, Object>> getLowStockProducts(
            @P("库存阈值，低于此值视为不足，默认 10") Integer threshold) {

        ToolGuard.requireLogin();
        int t = threshold == null ? 10 : threshold;

        Bill query = new Bill();
        query.setProductName("");
        query.setProviderId(0L);
        query.setIsPayment(0);
        List<Bill> all = billService.getBillList(query);
        if (all == null) return List.of();

        Map<String, BigDecimal> stockByName = all.stream()
                .filter(b -> b.getProductName() != null && b.getProductCount() != null)
                .collect(Collectors.groupingBy(
                        Bill::getProductName,
                        Collectors.reducing(BigDecimal.ZERO, Bill::getProductCount, BigDecimal::add)));

        return stockByName.entrySet().stream()
                .filter(e -> e.getValue().compareTo(BigDecimal.valueOf(t)) < 0)
                .sorted(Map.Entry.comparingByValue())
                .map(e -> Map.<String, Object>of(
                        "productName", e.getKey(),
                        "estimatedStock", e.getValue(),
                        "threshold", t))
                .limit(ToolGuard.MAX_PAGE_SIZE)
                .collect(Collectors.toList());
    }
}
