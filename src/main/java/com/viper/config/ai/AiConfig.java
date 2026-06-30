package com.viper.config.ai;

import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * LangChain4j 装配：
 *  - 仅在 smbms.ai.enabled=true 时装配，避免本地无 key 启动失败；
 *  - 用 StreamingChatLanguageModel 支撑 SSE 流式输出；
 *  - baseUrl 走 OpenAI 兼容协议，因此 DashScope / DeepSeek / Moonshot / OpenAI 都能跑。
 */
@Configuration
@EnableConfigurationProperties(AiProperties.class)
@ConditionalOnProperty(prefix = "smbms.ai", name = "enabled", havingValue = "true", matchIfMissing = false)
public class AiConfig {

    @Bean
    public OpenAiStreamingChatModel streamingChatModel(AiProperties props) {
        if (props.getApiKey() == null || props.getApiKey().isBlank()) {
            throw new IllegalStateException(
                    "smbms.ai.enabled=true 但未配置 smbms.ai.api-key（SMBMS_AI_API_KEY）");
        }
        return OpenAiStreamingChatModel.builder()
                .baseUrl(props.getBaseUrl())
                .apiKey(props.getApiKey())
                .modelName(props.getModel())
                .temperature(props.getTemperature())
                .timeout(Duration.ofSeconds(props.getTimeoutSeconds()))
                .logRequests(false)
                .logResponses(false)
                .build();
    }
}
