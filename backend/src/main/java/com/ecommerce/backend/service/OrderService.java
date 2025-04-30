package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.OrderItemResponse;
import com.ecommerce.backend.dto.OrderResponse;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.exception.UnauthorizedException;
import com.ecommerce.backend.model.Cart;
import com.ecommerce.backend.model.Order;
import com.ecommerce.backend.model.OrderItem;
import com.ecommerce.backend.model.OrderStatus;
import com.ecommerce.backend.model.PaymentMethod;
import com.ecommerce.backend.model.Product;
import com.ecommerce.backend.model.User;
import com.ecommerce.backend.repository.OrderRepository;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService
{

  private final OrderRepository orderRepository;
  private final UserRepository userRepository;
  private final CheckoutService checkoutService;
  private final UserService userService;
  private final ProductRepository productRepository;

  //we are blinding returning first order chance that user has multiple order , need to fix in long term
  public List<OrderResponse> getOrder(Long userId) {
    List<Order> orders = orderRepository.findByUserId(userId);
    List<OrderResponse> orderResponseList=new ArrayList<>();
    if (orders.isEmpty()) {
     /* orderResponseList.add(createNewOrder(userId));
      return orderResponseList; */
      throw new ResourceNotFoundException("Order", "userId", userId);
    } else {
      for (int i = 0; i <orders.size(); i++)
      {
        orderResponseList.add(mapToOrderResponse(orders.get(i)));
      }
    }
    return orderResponseList;
  }

  private OrderResponse createNewOrder(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    return checkoutService.checkout(userId, PaymentMethod.COD);
  }

  public OrderResponse getOrderById(Long userId, Long orderId) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

    if (!order.getUserId().equals(userId)) {
      throw new UnauthorizedException("You cannot access this order.");
    }

    return mapToOrderResponse(order);
  }


  public void clearOrder(Long userId, Long orderId) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

    if (!order.getId().equals(userId)) {
      throw new UnauthorizedException("You cannot cancel this order.");
    }

    order.setStatus(OrderStatus.CANCELLED); // If using status, otherwise delete
    orderRepository.save(order); // save the updated status
  }

  @Transactional
  public OrderResponse confirmOrderAndDeductInventory(Long userId, Long orderId) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

    if (!order.getUserId().equals(userId)) {
      throw new UnauthorizedException("You cannot confirm this order.");
    }

    if (order.getStatus() == OrderStatus.CONFIRMED) {
      throw new IllegalStateException("Order already confirmed.");
    }

    for (OrderItem item : order.getOrderItems()) {
      Product product = productRepository.findById(item.getProductId())
          .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

      if (product.getQuantity() < item.getQuantity()) {
        throw new RuntimeException("Insufficient stock for product: " + product.getName());
      }

      product.setQuantity(product.getQuantity() - item.getQuantity());
      productRepository.save(product);
    }

    order.setStatus(OrderStatus.CONFIRMED); // or whatever status means "confirmed"
    orderRepository.save(order);

    return mapToOrderResponse(order);
  }

  private OrderResponse mapToOrderResponse(Order order) {
    // You need to define this mapper.
    OrderResponse response = new OrderResponse();
    response.setId(order.getId());
    response.setStatus(order.getStatus());
    response.setTotalAmount(order.getTotalAmount());
    response.setPaymentMethod(order.getPaymentMethod());
    List<OrderItemResponse> list=new ArrayList<>();
    for (OrderItem oi : order.getOrderItems())
    {
      OrderItemResponse oir=new OrderItemResponse(
          oi.getId(),
          oi.getProductId(),
          oi.getQuantity(),
          oi.getPrice(),
      oi.getTotalPrice()
      );
      list.add(oir);
    }
    response.setList(list);
    return response;
  }
}

