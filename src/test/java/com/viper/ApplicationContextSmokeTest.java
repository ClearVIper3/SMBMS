package com.viper;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 烟囱测试：仅验证 Spring 上下文能正常启动。
 * <p>
 * 阶段 0 的最小回归网。后续 SB 2.7 → SB 3 升级时，此测试会第一时间暴露
 * 自动装配 / 依赖兼容性 / javax→jakarta 命名空间等问题。
 */
@SpringBootTest
class ApplicationContextSmokeTest {

    @Test
    void contextLoads() {
        // 仅触发上下文加载；任何 Bean 装配失败都会让此测试失败。
    }
}
