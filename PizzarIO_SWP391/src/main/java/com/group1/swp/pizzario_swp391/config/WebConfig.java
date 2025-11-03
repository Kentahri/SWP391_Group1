package com.group1.swp.pizzario_swp391.config;

import com.group1.swp.pizzario_swp391.config.interceptor.OrderFlowInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final OrderFlowInterceptor orderFlowInterceptor;

    public WebConfig(OrderFlowInterceptor orderFlowInterceptor) {
        this.orderFlowInterceptor = orderFlowInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(orderFlowInterceptor)
                .addPathPatterns("/guest/**", "/menu/**");
    }
}

