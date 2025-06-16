package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.AddressResponse;
import com.ecommerce.backend.model.Address;
import com.ecommerce.backend.model.Payment;
import com.ecommerce.backend.model.User;
import com.ecommerce.backend.repository.AddressRepository;
import com.ecommerce.backend.repository.PaymentRepository;
import com.ecommerce.backend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PaymentControllerIntegrationTest extends BaseIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private PaymentRepository paymentRepository;

  @Autowired
  private UserRepository userRepository;

  private static final Logger logger = LoggerFactory.getLogger(PaymentControllerIntegrationTest.class);

  @BeforeEach
  void setUp() {
    logger.warn("Running PaymentControllerIntegrationTest setUp 🚀");

    paymentRepository.deleteAllInBatch();
  }

  @AfterEach
  void cleanup() {
    logger.warn("Cleaning up PaymentControllerIntegrationTest ✅");

    paymentRepository.deleteAllInBatch();
  }

  @Test
  @WithMockUser(username = TEST_USERNAME)
  void testCreatePaymentOrder() throws Exception {
    logger.warn("Running testCreatePaymentOrder 🚀");

    mockMvc.perform(post("/payments")
            .param("amount", "500")
            .param("currency", "INR")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.amount").value(500.0))
        .andExpect(jsonPath("$.currency").value("INR"));

    logger.warn("testCreatePaymentOrder passed ✅");
  }

  @Test
  @WithMockUser(username = TEST_USERNAME)
  void testGetUserPaymentsInitiallyEmpty() throws Exception {
    logger.warn("Running testGetUserPaymentsInitiallyEmpty 🚀");

    mockMvc.perform(get("/payments/user")
            .param("page", "0")
            .param("size", "5")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(0)));

    logger.warn("testGetUserPaymentsInitiallyEmpty passed ✅");
  }

  @Test
  @WithMockUser(username = TEST_USERNAME)
  void testGetPayment() throws Exception {
    logger.warn("Running testGetPayment 🚀");

    User user = userRepository.findByUsername(TEST_USERNAME).get();

    // Insert Payment manually
    Payment payment = new Payment();
    payment.setUser(user);
    payment.setRazorpayOrderId("test_order_id_123");
    payment.setAmount(300.0);
    payment.setCurrency("INR");
    payment.setStatus("created");
    payment.setCreatedAt(LocalDateTime.now());

    paymentRepository.save(payment);

    mockMvc.perform(get("/payments/" + "test_order_id_123")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.razorpayOrderId").value("test_order_id_123"))
        .andExpect(jsonPath("$.amount").value(300.0))
        .andExpect(jsonPath("$.currency").value("INR"))
        .andExpect(jsonPath("$.status").value("created"));

    logger.warn("testGetPayment passed ✅");
  }
}

