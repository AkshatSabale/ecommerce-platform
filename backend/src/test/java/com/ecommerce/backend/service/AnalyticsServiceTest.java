package com.ecommerce.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.when;


import com.ecommerce.backend.dto.DailyRevenueDTO;
import com.ecommerce.backend.dto.RevenueResponse;
import com.ecommerce.backend.dto.TopProductDTO;
import com.ecommerce.backend.repository.OrderRepository;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;


@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

  @Mock
  private OrderRepository orderRepository;

  @Mock
  private MeterRegistry meterRegistry;

  @InjectMocks
  private AnalyticsService analyticsService;

  @Test
  void testGetRevenueAllTime() {
    when(orderRepository.findTotalRevenueAllTime()).thenReturn(Optional.of(1234.56));

    RevenueResponse response = analyticsService.getRevenueBetween(null, null);

    assertEquals(1234.56, response.getAmount());
    assertNull(response.getFromDate());
    assertNull(response.getToDate());
  }


  @Test
  void testDailyRevenueWithDefaults() {
    List<DailyRevenueDTO> expected = List.of(new DailyRevenueDTO(LocalDate.now(), 500.0));
    when(orderRepository.findDailyRevenueBetween(any(), any())).thenReturn(expected);

    List<DailyRevenueDTO> actual = analyticsService.getDailyRevenueBetween(null, null);

    assertEquals(expected.size(), actual.size());
    assertEquals(expected.get(0).getRevenue(), actual.get(0).getRevenue());
  }
}
