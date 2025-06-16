package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.DailyRevenueDTO;
import com.ecommerce.backend.dto.RevenueResponse;
import com.ecommerce.backend.dto.TopProductDTO;
import com.ecommerce.backend.service.AnalyticsService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

  private final AnalyticsService analyticsService;

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/top-products")
  public List<TopProductDTO> topProducts(
      @RequestParam(defaultValue = "10") int limit,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
    return analyticsService.getTopProducts(limit, startDate, endDate);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/revenue")
  public RevenueResponse revenue(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
    return analyticsService.getRevenueBetween(startDate, endDate);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/revenue/daily")
  public List<DailyRevenueDTO> dailyRevenue(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
    return analyticsService.getDailyRevenueBetween(startDate, endDate);
  }
}