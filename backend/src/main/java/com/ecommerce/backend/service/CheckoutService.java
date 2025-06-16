package com.ecommerce.backend.service;

import com.ecommerce.backend.controller.ProductController;
import com.ecommerce.backend.dto.AddressDto;
import com.ecommerce.backend.dto.CartItemDto;
import com.ecommerce.backend.dto.CartResponse;
import com.ecommerce.backend.dto.CheckoutRequest;
import com.ecommerce.backend.dto.OrderItemResponse;
import com.ecommerce.backend.dto.OrderResponse;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.model.Order;
import com.ecommerce.backend.model.OrderItem;
import com.ecommerce.backend.model.OrderStatus;
import com.ecommerce.backend.model.Payment;
import com.ecommerce.backend.model.PaymentMethod;
import com.ecommerce.backend.model.Product;
import com.ecommerce.backend.model.User;
import com.ecommerce.backend.repository.CartRepository;
import com.ecommerce.backend.repository.OrderRepository;
import com.ecommerce.backend.repository.PaymentRepository;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j;
import org.hibernate.internal.log.SubSystemLogging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CheckoutService {

  private final CartService cartService;
  private final CartRepository cartRepository;
  private final UserRepository userRepository;
  private final OrderRepository orderRepository;
  private final ProductRepository productRepository;
  private final PaymentRepository paymentRepository;
  private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

  @Transactional
  public OrderResponse checkout(Long userId, CheckoutRequest request) {
    // Step 1: Get Cart
    CartResponse cartResponse = cartService.getCart(userId);
    if (cartResponse.getItems().isEmpty()) {
      throw new IllegalStateException("Cart is empty, cannot checkout");
    }

    // Step 2: Build OrderItems
    List<OrderItem> orderItems = new ArrayList<>();
    double totalAmount = 0.0;
    for (CartItemDto cartItem : cartResponse.getItems()) {
      Product product = productRepository.findById(cartItem.getProductId())
          .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

      if (product.getQuantity() < cartItem.getQuantity()) {
        throw new IllegalStateException("Insufficient stock for product: " + product.getId());
      }

    //  product.setQuantity(product.getQuantity() - cartItem.getQuantity());
    //  productRepository.save(product);

      OrderItem orderItem = new OrderItem();
      orderItem.setProductId(cartItem.getProductId());
      orderItem.setQuantity(cartItem.getQuantity());
      orderItem.setPrice(cartItem.getProductPrice());
      orderItem.setTotalPrice(cartItem.getProductPrice() * cartItem.getQuantity());
      orderItems.add(orderItem);

      totalAmount += orderItem.getTotalPrice();
    }

    // Step 3: Create Order
    Order order = new Order();
    order.setUserId(userId);
    order.setTotalAmount(totalAmount);
    order.setStatus(OrderStatus.PENDING);
    order.setCreatedAt(LocalDateTime.now());
    order.setPaymentMethod(request.getPaymentMethod());
    order.setOrderItems(orderItems);
    order.setDoorNumber(request.getAddress().getDoorNumber());
    order.setAddressLine1(request.getAddress().getAddressLine1());
    order.setAddressLine2(request.getAddress().getAddressLine2());
    order.setPinCode(request.getAddress().getPinCode());
    order.setCity(request.getAddress().getCity());

    for (OrderItem item : orderItems) {
      item.setOrder(order);
    }

    // Step 4: Save Order first (so it gets an ID)
    Order savedOrder = orderRepository.saveAndFlush(order);

    // Step 5: If not COD, create payment and associate
    if (request.getPaymentMethod() != PaymentMethod.COD) {
      User user = userRepository.findById(userId)
          .orElseThrow(() -> new ResourceNotFoundException("User not found"));

      Payment payment = paymentRepository.findByRazorpayOrderId(request.getOrderId())
          .orElseThrow(() -> new IllegalStateException("Payment not found for order: " + request.getOrderId()));
      order.setPayment(payment);
      payment.setPaymentId(request.getPaymentId());
      payment.setStatus("PAID");

      // CRITICAL: Set the order on payment
      payment.setOrder(savedOrder);

      // DEBUG: Verify the order ID is set
      logger.debug("Payment order ID before save: {}",
          payment.getOrder() != null ? payment.getOrder().getId() : "null");


      savedOrder.setPayment(payment);

      paymentRepository.save(payment);
      // Update the order's payment reference
    }


    // Step 6: Clear Cart
    cartService.clearCart(userId);

    // Step 7: Build Response
    List<OrderItemResponse> orderItemResponses = savedOrder.getOrderItems().stream()
        .map(oi -> new OrderItemResponse(
            oi.getId(),
            oi.getProductId(),
            oi.getQuantity(),
            oi.getPrice(),
            oi.getTotalPrice()
        ))
        .collect(Collectors.toList());

    AddressDto addressDto = new AddressDto();
    addressDto.setDoorNumber(savedOrder.getDoorNumber());
    addressDto.setAddressLine1(savedOrder.getAddressLine1());
    addressDto.setAddressLine2(savedOrder.getAddressLine2());
    addressDto.setCity(savedOrder.getCity());
    addressDto.setPinCode(savedOrder.getPinCode());

    return new OrderResponse(
        savedOrder.getId(),
        orderItemResponses,
        savedOrder.getStatus(),
        savedOrder.getTotalAmount(),
        savedOrder.getPaymentMethod(),
        addressDto,
        savedOrder.getCreatedAt()
    );
  }
}


