package com.ecommerce.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


import com.ecommerce.backend.model.Payment;
import com.ecommerce.backend.model.User;
import com.ecommerce.backend.repository.PaymentRepository;
import com.ecommerce.backend.repository.UserRepository;
import com.razorpay.Order;
import com.razorpay.OrderClient;
import com.razorpay.RazorpayClient;
import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import org.springframework.test.annotation.Rollback;

@ExtendWith(MockitoExtension.class)
@Rollback
class PaymentServiceTest {

  @InjectMocks
  private PaymentService paymentService;

  @Mock
  private RazorpayClient razorpayClient;

  @Mock
  private OrderClient orderClient; // ADD THIS LINE

  @Mock
  private PaymentRepository paymentRepository;

  @Mock
  private UserRepository userRepository;

  @BeforeEach
  void setUp() throws Exception {
    LoggerFactory.getLogger(PaymentServiceTest.class)
        .warn("Running PaymentServiceTest setUp 🚀");

    // Use reflection to set the orders field
    Field ordersField = RazorpayClient.class.getDeclaredField("orders");
    ordersField.setAccessible(true);
    ordersField.set(razorpayClient, orderClient);
  }

  @Test
  void testCreateAndSavePaymentOrder() throws Exception {
    LoggerFactory.getLogger(PaymentServiceTest.class)
        .warn("Running testCreateAndSavePaymentOrder 🚀");

    Long userId = 1L;

    User user = new User();
    user.setId(userId);
    user.setUsername("testuser");

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    // Mock Razorpay order
    Order mockOrder = mock(Order.class);
    when(mockOrder.get("id")).thenReturn("order_xyz_123");
    when(mockOrder.get("receipt")).thenReturn("receipt_123");

    // Use orderClient instead of razorpayClient.orders
    when(orderClient.create(any())).thenReturn(mockOrder);

    Payment savedPayment = new Payment();
    savedPayment.setId(10L);
    savedPayment.setRazorpayOrderId("order_xyz_123");
    savedPayment.setAmount(100.0);
    savedPayment.setCurrency("INR");
    savedPayment.setStatus("created");

    when(paymentRepository.save(any())).thenReturn(savedPayment);

    Payment payment = paymentService.createAndSavePaymentOrder(100.0, "INR", userId);

    assertNotNull(payment);
    assertEquals("order_xyz_123", payment.getRazorpayOrderId());
    assertEquals(100.0, payment.getAmount());
    assertEquals("INR", payment.getCurrency());
    assertEquals("created", payment.getStatus());

    verify(paymentRepository, times(1)).save(any());
    LoggerFactory.getLogger(PaymentServiceTest.class)
        .warn("testCreateAndSavePaymentOrder passed ✅");
  }

  @Test
  void testGetPaymentForUser_Success() {
    LoggerFactory.getLogger(PaymentServiceTest.class)
        .warn("Running testGetPaymentForUser_Success 🚀");

    Long userId = 1L;
    String paymentId = "order_xyz_123";

    User user = new User();
    user.setId(userId);

    Payment payment = new Payment();
    payment.setRazorpayOrderId(paymentId);
    payment.setUser(user);

    when(paymentRepository.findByRazorpayOrderId(paymentId)).thenReturn(Optional.of(payment));

    Payment fetchedPayment = paymentService.getPaymentForUser(paymentId, userId);

    assertNotNull(fetchedPayment);
    assertEquals(paymentId, fetchedPayment.getRazorpayOrderId());

    LoggerFactory.getLogger(PaymentServiceTest.class)
        .warn("testGetPaymentForUser_Success passed ✅");
  }

  @Test
  void testGetPaymentForUser_AccessDenied() {
    LoggerFactory.getLogger(PaymentServiceTest.class)
        .warn("Running testGetPaymentForUser_AccessDenied 🚀");

    Long userId = 1L;
    String paymentId = "order_xyz_123";

    User user = new User();
    user.setId(99L); // Different userId

    Payment payment = new Payment();
    payment.setRazorpayOrderId(paymentId);
    payment.setUser(user);

    when(paymentRepository.findByRazorpayOrderId(paymentId)).thenReturn(Optional.of(payment));

    assertThrows(RuntimeException.class, () -> paymentService.getPaymentForUser(paymentId, userId));

    LoggerFactory.getLogger(PaymentServiceTest.class)
        .warn("testGetPaymentForUser_AccessDenied passed ✅");
  }
}
