package com.viper.service.ai.tool;

import com.viper.pojo.Bill;
import com.viper.service.bill.BillService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class BillQueryTool {

    private final BillService billService;

    public BillQueryTool(BillService billService) {
        this.billService = billService;
    }

    @Tool(name = "listOrders",
            value = "查询订单列表，可按商品名模糊、供应商 id、支付状态过滤。" +
                    "支付状态: 0=全部 1=未付款 2=已付款。" +
                    "当用户问'最近的订单''哪些订单还没付''供应商X有哪些订单'时使用。")
    public List<Map<String, Object>> listOrders(
            @P("商品名关键字，可空") String productName,
            @P("供应商 id，0 表示全部") Long providerId,
            @P("支付状态：0=全部 1=未付款 2=已付款") Integer payment) {

        ToolGuard.requireLogin();
        Bill q = new Bill();
        q.setProductName(productName == null ? "" : productName);
        q.setProviderId(providerId == null ? 0L : providerId);
        q.setIsPayment(payment == null ? 0 : payment);
        List<Bill> list = billService.getBillList(q);
        if (list == null) return List.of();
        return list.stream().limit(ToolGuard.MAX_PAGE_SIZE).map(BillQueryTool::toMap).collect(Collectors.toList());
    }

    @Tool(name = "getOrderInfo",
            value = "根据订单主键 id 查询订单详细信息。")
    public Map<String, Object> getOrderInfo(@P("订单主键 id") Long orderId) {
        ToolGuard.requireLogin();
        Bill b = billService.getBillById(String.valueOf(orderId));
        if (b == null) return Map.of("found", false);
        Map<String, Object> m = new java.util.HashMap<>(toMap(b));
        m.put("found", true);
        return m;
    }

    private static Map<String, Object> toMap(Bill b) {
        return Map.of(
                "id", b.getId(),
                "billCode", b.getBillCode(),
                "productName", n(b.getProductName()),
                "productUnit", n(b.getProductUnit()),
                "productCount", b.getProductCount(),
                "totalPrice", b.getTotalPrice(),
                "isPayment", b.getIsPayment() == null ? 0
                        : (b.getIsPayment() == 2 ? "已付款" : "未付款"),
                "providerId", b.getProviderId(),
                "providerName", n(b.getProviderName())
        );
    }

    private static String n(String s) { return s == null ? "" : s; }
}
