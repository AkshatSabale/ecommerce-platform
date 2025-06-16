package com.ecommerce.backend.component;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class CacheLoggerAspect {

  private static final Logger logger = LoggerFactory.getLogger(CacheLoggerAspect.class);

  @Around("@annotation(org.springframework.cache.annotation.Cacheable)")
  public Object logCacheActivity(ProceedingJoinPoint pjp) throws Throwable {
    String cacheName = pjp.getSignature().toShortString();
    logger.info("Checking cache for {}", cacheName);

    try {
      Object result = pjp.proceed();
      logger.info("Cache hit for {}", cacheName);
      return result;
    } catch (Exception e) {
      logger.info("Cache miss for {}, fetching from source", cacheName);
      throw e;
    }
  }
}
