package org.app.f1.config;

import lombok.RequiredArgsConstructor;
import org.app.f1.filter.RateLimiter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RateLimiterConfig {

    private final RateLimiter rateLimiter;

    @Bean
    public FilterRegistrationBean<RateLimiter> rateLimitingFilter() {
        FilterRegistrationBean<RateLimiter> registration = new FilterRegistrationBean<>();
        registration.setFilter(rateLimiter);
        registration.addUrlPatterns("/api/*");
        registration.setOrder(1);
        return registration;
    }
}