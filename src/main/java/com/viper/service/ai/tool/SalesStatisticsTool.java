package com.viper.service.ai.tool;

import com.viper.pojo.Bill;
import com.viper.service.bill.BillService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 销售统计工具。
 * 口径：把 smbms_bill 视为订单事实表，totalPrice 视为该单销售额，
 * creationDate 视为成交时间；按日期范围聚合。
 */
@Component
public class SalesStatisticsTool {

    private final BillService billService;

    public SalesStatisticsTool(BillService billService) {
        this.billService = billService;
    }

    @Tool(name = "getSalesStatistics",
            value = "查询指定日期范围内的销售统计，返回：总销售额、订单数、TOP5 商品（按销售额降序）、TOP5 供应商。" +
                    "日期参数格式 YYYY-MM-DD。" +
                    "当用户问'最近7天销售额''本月卖最好的商品''某段时间销售情况'时使用。")
    public Map<String, Object> getSalesStatistics(
            @P("起始日期 YYYY-MM-DD，含当天") String startDate,
            @P("结束日期 YYYY-MM-DD，含当天") String endDate) {

        ToolGuard.requireLogin();
        LocalDate from = LocalDate.parse(startDate);
        LocalDate to   = LocalDate.parse(endDate);
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("startDate 不能晚于 endDate");
        }
        ZoneId zone = ZoneId.systemDefault();

        Bill query = new Bill();
        query.setProductName(""); query.setProviderId(0L); query.setIsPayment(0);
        List<Bill> all = billService.getBillList(query);

        List<Bill> inRange = all == null ? List.of() : all.stream()
                .filter(b -> b.getCreationDate() != null && b.getTotalPrice() != null)
                .filter(b -> {
                    LocalDate d = b.getCreationDate().toInstant().atZone(zone).toLocalDate();
                    return !d.isBefore(from) && !d.isAfter(to);
                })
                .collect(Collectors.toList());

        BigDecimal total = inRange.stream().map(Bill::getTotalPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        int orderCount = inRange.size();

        List<Map<String, Object>> topProducts = inRange.stream()
                .filter(b -> b.getProductName() != null)
                .collect(Collectors.groupingBy(Bill::getProductName,
                        Collectors.reducing(BigDecimal.ZERO, Bill::getTotalPrice, BigDecimal::add)))
                .entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(5)
                .map(e -> Map.<String, Object>of("productName", e.getKey(), "salesAmount", e.getValue()))
                .collect(Collectors.toList());

        List<Map<String, Object>> topProviders = inRange.stream()
                .filter(b -> b.getProviderName() != null)
                .collect(Collectors.groupingBy(Bill::getProviderName,
                        Collectors.reducing(BigDecimal.ZERO, Bill::getTotalPrice, BigDecimal::add)))
                .entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(5)
                .map(e -> Map.<String, Object>of("providerName", e.getKey(), "salesAmount", e.getValue()))
                .collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("startDate", startDate);
        result.put("endDate", endDate);
        result.put("totalSales", total);
        result.put("orderCount", orderCount);
        result.put("topProducts", topProducts);
        result.put("topProviders", topProviders);
        return result;
    }
}
