package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.TopProductDTO;
import com.ecommerce.backend.repository.OrderRepository;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnalyticsService {


  private final OrderRepository orderRepo;
  private final MeterRegistry meterRegistry;

  public List<TopProductDTO> getTopProducts(int limit, int days) {
    Pageable page = PageRequest.of(0, limit);
    meterRegistry.counter("analytics.topProducts.calls").increment();
    if (days > 0) {
      return orderRepo.findTopSellingProductsSince(LocalDateTime.now().minusDays(days), page);
    } else {
      return orderRepo.findTopSellingProductsAllTime(page);
    }
  }

  public double getRevenueSince(int days) {
    double revenue = orderRepo
        .findTotalRevenueSince(LocalDateTime.now().minusDays(days))
        .orElse(0.0);

    // expose gauge “revenue.lastXdays”
    meterRegistry.gauge("revenue.last" + days + "days", revenue);

    return revenue;
  }
}
