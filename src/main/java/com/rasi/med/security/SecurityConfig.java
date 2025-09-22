package com.rasi.med.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.*;
import org.springframework.core.Ordered;

@Configuration
@Profile("central") // solo en la central
public class SecurityConfig {

    @Bean
    public FilterRegistrationBean<ApiKeyFilter> apiKeyFilterRegistration(
            org.springframework.core.env.Environment env) {
        String apiKey = env.getProperty("sync.apiKey", "dev-key-change-me");
        FilterRegistrationBean<ApiKeyFilter> reg = new FilterRegistrationBean<>();
        reg.setFilter(new ApiKeyFilter(apiKey));
        reg.addUrlPatterns("/api/sync/*");
        reg.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return reg;
    }
}
