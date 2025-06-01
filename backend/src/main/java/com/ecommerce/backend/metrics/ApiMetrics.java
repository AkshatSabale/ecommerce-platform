package com.ecommerce.backend.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import org.springframework.stereotype.Component;

@Component
public class ApiMetrics {
  private final Counter apiRequestCounter;
  private final Timer apiResponseTimer;

  public ApiMetrics(MeterRegistry registry) {
    apiRequestCounter = Counter.builder("api.requests.total")
        .description("Total API requests")
        .tags("application", "ecommerce-backend")
        .register(registry);

    apiResponseTimer = Timer.builder("api.response.time")
        .description("API response time in milliseconds")
        .tags("application", "ecommerce-backend")
        .register(registry);
  }

  public void incrementRequestCount(String endpoint) {
    apiRequestCounter.increment();
  }

  public void recordResponseTime(String endpoint, long duration) {
    apiResponseTimer.record(Duration.ofMillis(duration));
  }
}