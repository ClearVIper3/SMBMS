package com.viper.config;

import com.viper.filter.SysFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("redirect:/login");
        registry.addViewController("/login").setViewName("login");
        registry.addViewController("/frame").setViewName("frame");
    }

    @Bean
    public FilterRegistrationBean<SysFilter> sysFilter() {
        FilterRegistrationBean<SysFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new SysFilter());
        registrationBean.addUrlPatterns("/bill/*", "/provider/*", "/user/*", "/frame");
        return registrationBean;
    }
}
