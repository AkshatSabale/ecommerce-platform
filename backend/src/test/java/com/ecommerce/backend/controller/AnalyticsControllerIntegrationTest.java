package com.ecommerce.backend.controller;

import com.ecommerce.backend.model.Order;
import com.ecommerce.backend.model.OrderItem;
import com.ecommerce.backend.model.OrderStatus;
import com.ecommerce.backend.repository.OrderRepository;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AnalyticsControllerIntegrationTest extends BaseIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private OrderRepository orderRepository;

  @Autowired
  private MeterRegistry meterRegistry;

  private static final Logger logger = LoggerFactory.getLogger(WishlistControllerIntegrationTest.class);

  @BeforeEach
  void setupOrders() {
    logger.warn("Setting up orders for analytics tests 🚀");

    orderRepository.deleteAll();

    Order order = new Order();
    order.setUserId(userRepository.findByUsername(TEST_USERNAME).orElseThrow().getId());
    order.setStatus(OrderStatus.DELIVERED);
    order.setTotalAmount(500.0);
    order.setCreatedAt(LocalDateTime.now().minusDays(5));

    OrderItem item = new OrderItem();
    item.setProductId(1L);
    item.setQuantity(5);
    item.setPrice(100.0);
    item.setOrder(order);

    order.setOrderItems(List.of(item));
    orderRepository.save(order);

    logger.warn("Orders setup complete ✅");
  }

  @Test
  @WithMockUser(username = TEST_USERNAME, roles = {"ADMIN"})
  void testTopProductsEndpoint() throws Exception {
    mockMvc.perform(get("/api/analytics/top-products?limit=5"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));
  }

  @Test
  @WithMockUser(username = TEST_USERNAME, roles = {"ADMIN"})
  void testRevenueEndpoint() throws Exception {
    mockMvc.perform(get("/api/analytics/revenue"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.amount").value(500.0));
  }

  @Test
  @WithMockUser(username = TEST_USERNAME, roles = {"ADMIN"})
  void testDailyRevenueEndpoint() throws Exception {
    mockMvc.perform(get("/api/analytics/revenue/daily"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].revenue").value(500.0));
  }

  @Test
  @WithMockUser(username = TEST_USERNAME, roles = {"CUSTOMER"}) // no ADMIN
  void testForbiddenAccess() throws Exception {
    mockMvc.perform(get("/api/analytics/revenue"))
        .andExpect(status().isForbidden());
  }
}
