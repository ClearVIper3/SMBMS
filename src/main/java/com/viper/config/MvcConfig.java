package com.viper.config;

import com.viper.security.JwtAuthFilter;
import com.viper.security.JwtService;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * MVC 配置：
 *  - 注册 JWT 鉴权过滤器（仅作用于 /api/**）
 *  - 开启 /api/** CORS（前端独立部署，跨域必备）
 */
@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Bean
    public FilterRegistrationBean<JwtAuthFilter> jwtAuthFilter(JwtService jwtService) {
        FilterRegistrationBean<JwtAuthFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new JwtAuthFilter(jwtService));
        bean.addUrlPatterns("/api/*");
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        return bean;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Authorization")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
