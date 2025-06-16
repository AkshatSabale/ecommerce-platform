package com.ecommerce.backend.filters;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class RateLimitingFilter implements Filter {

  private final Map<String, Bucket> loginBuckets = new ConcurrentHashMap<>();
  private final Map<String, Bucket> searchBuckets = new ConcurrentHashMap<>();

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) request;
    String ip = request.getRemoteAddr();
    String path = req.getRequestURI();

    boolean allowed = true;

    if (path.startsWith("/api/auth/login")) {
      allowed = checkRateLimit(loginBuckets, ip, 5, Duration.ofMinutes(10));
    } else if (path.startsWith("/api/products/search")) {
      allowed = checkRateLimit(searchBuckets, ip, 20, Duration.ofMinutes(1));
    }

    if (!allowed) {
      HttpServletResponse httpResp = (HttpServletResponse) response;
      httpResp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      httpResp.setContentType("application/json");
      httpResp.getWriter().write("{\"message\": \"Too many requests. Please try again later.\"}");
      return;
    }

    chain.doFilter(request, response);
  }

  private boolean checkRateLimit(Map<String, Bucket> buckets, String ip, int maxRequests, Duration duration) {
    Bucket bucket = buckets.computeIfAbsent(ip, k -> {
      Refill refill = Refill.greedy(maxRequests, duration);
      Bandwidth limit = Bandwidth.classic(maxRequests, refill);
      return Bucket4j.builder().addLimit(limit).build();
    });
    return bucket.tryConsume(1);
  }
}
