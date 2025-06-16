package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.DailyRevenueDTO;
import com.ecommerce.backend.dto.RevenueResponse;
import com.ecommerce.backend.dto.TopProductDTO;
import com.ecommerce.backend.repository.OrderRepository;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.LocalDate;
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

  public List<TopProductDTO> getTopProducts(int limit, LocalDate startDate, LocalDate endDate) {
    Pageable page = PageRequest.of(0, limit);
    meterRegistry.counter("analytics.topProducts.calls").increment();

    if (startDate != null && endDate != null) {
      return orderRepo.findTopSellingProductsBetween(
          startDate.atStartOfDay(),
          endDate.atTime(23, 59, 59),
          page
      );
    } else if (startDate != null) {
      return orderRepo.findTopSellingProductsSince(startDate.atStartOfDay(), page);
    } else {
      return orderRepo.findTopSellingProductsAllTime(page);
    }
  }

  public RevenueResponse getRevenueBetween(LocalDate startDate, LocalDate endDate) {
    Double revenue;

    if (startDate != null && endDate != null) {
      revenue = orderRepo.findTotalRevenueBetween(
          startDate.atStartOfDay(),
          endDate.atTime(23, 59, 59)
      ).orElse(0.0);
    } else if (startDate != null) {
      revenue = orderRepo.findTotalRevenueSince(startDate.atStartOfDay()).orElse(0.0);
    } else {
      revenue = orderRepo.findTotalRevenueAllTime().orElse(0.0);
    }

    // Expose gauge
    String gaugeName = "revenue";
    if (startDate != null) gaugeName += ".from_" + startDate;
    if (endDate != null) gaugeName += ".to_" + endDate;
    meterRegistry.gauge(gaugeName, revenue);

    return new RevenueResponse(revenue, startDate, endDate);
  }

  public List<DailyRevenueDTO> getDailyRevenueBetween(LocalDate startDate, LocalDate endDate) {
    LocalDate defaultStart = LocalDate.now().minusDays(30);
    LocalDate defaultEnd = LocalDate.now();

    return orderRepo.findDailyRevenueBetween(
        (startDate != null ? startDate : defaultStart).atStartOfDay(),
        (endDate != null ? endDate : defaultEnd).atTime(23, 59, 59)
    );
  }
}
