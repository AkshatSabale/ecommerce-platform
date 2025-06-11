package com.ecommerce.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


import com.ecommerce.backend.dto.OrderResponse;
import com.ecommerce.backend.dto.ReturnRequestDto;
import com.ecommerce.backend.kafka.OrderMessage;
import com.ecommerce.backend.kafka.OrderProducer;
import com.ecommerce.backend.model.Order;
import com.ecommerce.backend.model.OrderStatus;
import com.ecommerce.backend.repository.OrderRepository;
import com.ecommerce.backend.repository.PaymentRepository;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.repository.UserRepository;
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
class OrderServiceTest {

  @InjectMocks
  private OrderService orderService;

  @Mock
  private OrderRepository orderRepository;

  @Mock
  private OrderProducer orderProducer;

  @Mock
  private ProductRepository productRepository;

  @Mock
  private PaymentRepository paymentRepository;

  @Mock
  private UserRepository userRepository;

  private static final Logger logger = LoggerFactory.getLogger(OrderServiceTest.class);

  @BeforeEach
  void setUp() {
    logger.warn("Running OrderServiceTest setUp 🚀");
  }

  @Test
  void testConfirmOrder() {
    logger.warn("Running testConfirmOrder 🚀");

    Long orderId = 1L;
    Order order = new Order();
    order.setId(orderId);
    order.setStatus(OrderStatus.PENDING);

    when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

    OrderResponse response = orderService.confirmOrder(orderId, OrderStatus.CONFIRMED);

    verify(orderProducer).sendMessage(any());
    assertEquals(OrderStatus.CONFIRMED, response.getStatus());

    logger.warn("testConfirmOrder passed ✅");
  }

  @Test
  void testShipOrder() {
    logger.warn("Running testShipOrder 🚀");

    Long orderId = 2L;
    Order order = new Order();
    order.setId(orderId);
    order.setStatus(OrderStatus.CONFIRMED);

    when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

    OrderResponse response = orderService.shipOrder(orderId, OrderStatus.SHIPPED);

    verify(orderProducer).sendMessage(any());
    assertEquals(OrderStatus.SHIPPED, response.getStatus());

    logger.warn("testShipOrder passed ✅");
  }

  @Test
  void testDeliverOrder() {
    logger.warn("Running testDeliverOrder 🚀");

    Long orderId = 3L;
    Order order = new Order();
    order.setId(orderId);
    order.setStatus(OrderStatus.SHIPPED);
    order.setUserId(42L);

    when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

    OrderResponse response = orderService.deliverOrder(orderId, OrderStatus.DELIVERED);

    verify(orderProducer).sendMessage(any());
    assertEquals(OrderStatus.DELIVERED, response.getStatus());

    logger.warn("testDeliverOrder passed ✅");
  }

  @Test
  void testApproveReturn() {
    logger.warn("Running testApproveReturn 🚀");

    Long orderId = 4L;

    // Call the service method (note: no mocking needed since no DB fetch in approveReturn)
    orderService.approveReturn(orderId);

    // Verify Kafka message sent
    ArgumentCaptor<OrderMessage> messageCaptor = ArgumentCaptor.forClass(OrderMessage.class);
    verify(orderProducer).sendMessage(messageCaptor.capture());

    OrderMessage message = messageCaptor.getValue();
    assertEquals("APPROVE_RETURN", message.getOperation());
    assertEquals(orderId, message.getOrderId());

    logger.warn("testApproveReturn passed ✅");
  }

  @Test
  void testCompleteReturn() {
    logger.warn("Running testCompleteReturn 🚀");

    Long orderId = 5L;

    orderService.completeReturn(orderId);

    ArgumentCaptor<OrderMessage> messageCaptor = ArgumentCaptor.forClass(OrderMessage.class);
    verify(orderProducer).sendMessage(messageCaptor.capture());

    OrderMessage message = messageCaptor.getValue();
    assertEquals("COMPLETE_RETURN", message.getOperation());
    assertEquals(orderId, message.getOrderId());

    logger.warn("testCompleteReturn passed ✅");
  }

  @Test
  void testRequestReturn() {
    logger.warn("Running testRequestReturn 🚀");

    Long orderId = 6L;
    Long userId = 10L;

    ReturnRequestDto requestDto = new ReturnRequestDto();
    requestDto.setReason("Defective item");

    orderService.requestReturn(orderId, requestDto, userId);

    ArgumentCaptor<OrderMessage> messageCaptor = ArgumentCaptor.forClass(OrderMessage.class);
    verify(orderProducer).sendMessage(messageCaptor.capture());

    OrderMessage message = messageCaptor.getValue();
    assertEquals("RETURN_REQUEST", message.getOperation());
    assertEquals(orderId, message.getOrderId());
    assertEquals(userId, message.getUserId());
    assertEquals("Defective item", message.getReturnRequest().getReason());

    logger.warn("testRequestReturn passed ✅");
  }
}
