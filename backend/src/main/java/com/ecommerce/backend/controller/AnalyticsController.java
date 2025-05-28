package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.TopProductDTO;
import com.ecommerce.backend.service.AnalyticsService;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
      @RequestParam(defaultValue = "0") int days) {
    return analyticsService.getTopProducts(limit, days);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/revenue")
  public double revenue(@RequestParam(defaultValue = "30") int days) {
    return analyticsService.getRevenueSince(days);
  }
}