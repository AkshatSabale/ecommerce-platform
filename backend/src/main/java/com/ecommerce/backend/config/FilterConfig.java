package com.ecommerce.backend.config;

import com.ecommerce.backend.filters.RateLimitingFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

  @Bean
  public FilterRegistrationBean<RateLimitingFilter> rateLimitingFilter() {
    FilterRegistrationBean<RateLimitingFilter> registrationBean = new FilterRegistrationBean<>();
    registrationBean.setFilter(new RateLimitingFilter());
    registrationBean.addUrlPatterns("/api/*"); // Apply to all API routes
    registrationBean.setOrder(1); // Order of execution if multiple filters
    return registrationBean;
  }
}