package com.viper.config.ai;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** 从 application.yaml 的 smbms.ai.* 装载。 */
@Data
@ConfigurationProperties(prefix = "smbms.ai")
public class AiProperties {
    /** 总开关。关闭后 /api/ai/** 返回 503，避免无 key 时硬启动 */
    private boolean enabled = true;
    private String baseUrl;
    private String apiKey;
    private String model;
    private Double temperature = 0.3;
    private Integer memoryWindow = 20;
    private Integer timeoutSeconds = 60;
    private String systemPrompt;
}
