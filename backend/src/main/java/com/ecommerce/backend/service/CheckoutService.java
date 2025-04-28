package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.CartItemDto;
import com.ecommerce.backend.dto.CartResponse;
import com.ecommerce.backend.dto.OrderItemResponse;
import com.ecommerce.backend.dto.OrderResponse;
import com.ecommerce.backend.model.Cart;
import com.ecommerce.backend.model.Order;
import com.ecommerce.backend.model.OrderItem;
import com.ecommerce.backend.model.OrderStatus;
import com.ecommerce.backend.model.PaymentMethod;
import com.ecommerce.backend.repository.CartRepository;
import com.ecommerce.backend.repository.OrderRepository;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CheckoutService {

  private final CartService cartService;
  private final CartRepository cartRepository;
  private final UserRepository userRepository;
  private final OrderRepository orderRepository;

  public OrderResponse checkout(Long userId, PaymentMethod paymentMethod) {
    // Step 1: Get Cart
    CartResponse cartResponse = cartService.getCart(userId);

    if (cartResponse.getItems().isEmpty()) {
      throw new IllegalStateException("Cart is empty, cannot checkout");
    }

    // Step 2: Build OrderItems
    List<OrderItem> orderItems = new ArrayList<>();
    double totalAmount = 0.0;

    for (CartItemDto cartItem : cartResponse.getItems()) {
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
    order.setStatus(OrderStatus.PENDING);   // Default status
    order.setCreatedAt(LocalDateTime.now());
    order.setPaymentMethod(paymentMethod);
    order.setOrderItems(orderItems);

    // Link back each orderItem to the order
    for (OrderItem orderItem : orderItems) {
      orderItem.setOrder(order);
    }

    // Step 4: Save Order
    Order savedOrder = orderRepository.save(order);

    // Step 5: Clear Cart
    cartService.clearCart(userId);

    // Step 6: Build Response
    List<OrderItemResponse> orderItemResponses = savedOrder.getOrderItems().stream()
        .map(oi -> new OrderItemResponse(
            oi.getId(),
            oi.getProductId(),
            oi.getQuantity(),
            oi.getPrice(),
            oi.getTotalPrice()
        ))
        .collect(Collectors.toList());

    return new OrderResponse(
        savedOrder.getId(),
        orderItemResponses,
        savedOrder.getStatus(),
        savedOrder.getTotalAmount(),
        savedOrder.getPaymentMethod()
    );
  }
}
