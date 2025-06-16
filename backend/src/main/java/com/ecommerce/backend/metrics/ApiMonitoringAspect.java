package com.ecommerce.backend.metrics;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class ApiMonitoringAspect {
  private final ApiMetrics apiMetrics;

  @Around("execution(* com.ecommerce.backend.controller..*.*(..))")
  public Object monitorApiCall(ProceedingJoinPoint joinPoint) throws Throwable {
    String endpoint = joinPoint.getSignature().toShortString();
    long startTime = System.currentTimeMillis();

    try {
      Object result = joinPoint.proceed();
      long duration = System.currentTimeMillis() - startTime;

      apiMetrics.incrementRequestCount(endpoint);
      apiMetrics.recordResponseTime(endpoint, duration);

      return result;
    } catch (Exception e) {
      apiMetrics.incrementRequestCount(endpoint + "_error");
      throw e;
    }
  }
}