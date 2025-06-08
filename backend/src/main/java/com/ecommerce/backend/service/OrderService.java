package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.AddressDto;
import com.ecommerce.backend.dto.OrderItemResponse;
import com.ecommerce.backend.dto.OrderResponse;
import com.ecommerce.backend.dto.ReturnRequestDto;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.exception.UnauthorizedException;
import com.ecommerce.backend.kafka.OrderMessage;
import com.ecommerce.backend.kafka.OrderProducer;
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
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

  private final OrderRepository orderRepository;
  private final UserRepository userRepository;
  private final CheckoutService checkoutService;
  private final UserService userService;
  private final ProductRepository productRepository;
  private final OrderProducer orderProducer;


  public List<OrderResponse> getOrder(Long userId) {
    List<Order> orders = orderRepository.findByUserId(userId);
    if (orders.isEmpty()) {
      throw new ResourceNotFoundException("Order", "userId", userId);
    }
    return orders.stream().map(this::mapToOrderResponse).collect(Collectors.toList());
  }



  public OrderResponse getOrderById(Long userId, Long orderId) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

    if (!order.getUserId().equals(userId)) {
      throw new UnauthorizedException("You cannot access this order.");
    }

    return mapToOrderResponse(order);
  }

  public Page<OrderResponse> getAllOrders(OrderStatus status, Pageable pageable) {
    Page<Order> orders;
    if (status != null) {
      orders = orderRepository.findByStatus(status, pageable);
    } else {
      orders = orderRepository.findAll(pageable);
    }

    // This is correct — Page.map(...) returns a Page<T>
    return orders.map(this::mapToOrderResponse);
  }


  public void clearOrder(Long userId, Long orderId) {
    OrderMessage message = new OrderMessage();
    message.setOperation("CANCEL");
    message.setOrderId(orderId);
    message.setUserId(userId);
    orderProducer.sendMessage(message);
  }


  public boolean requestReturn(Long orderId, ReturnRequestDto returnRequest, Long userId) {
    OrderMessage message = new OrderMessage();
    message.setOperation("RETURN_REQUEST");
    message.setOrderId(orderId);
    message.setUserId(userId);
    message.setReturnRequest(returnRequest);
    orderProducer.sendMessage(message);
    return true; // Assuming the request will be processed successfully
  }


  public boolean approveReturn(Long orderId) {
    OrderMessage message = new OrderMessage();
    message.setOperation("APPROVE_RETURN");
    message.setOrderId(orderId);
    orderProducer.sendMessage(message);
    return true; // Assuming the approval will be processed successfully
  }


  public OrderResponse confirmOrderAndDeductInventory(Long userId, Long orderId) {
    OrderMessage message = new OrderMessage();
    message.setOperation("CONFIRM");
    message.setOrderId(orderId);
    message.setUserId(userId);
    orderProducer.sendMessage(message);

    // Return a response immediately, actual processing will happen async
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
    return mapToOrderResponse(order);
  }

  public Order getOrderEntityById(Long userId, Long orderId) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

    if (!order.getUserId().equals(userId)) {
      throw new UnauthorizedException("You cannot access this order.");
    }

    return order;
  }

  public Order getOrderEntityById(Long orderId) {
    return orderRepository.findById(orderId)
        .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
  }


  public boolean completeReturn(Long orderId) {
    OrderMessage message = new OrderMessage();
    message.setOperation("COMPLETE_RETURN");
    message.setOrderId(orderId);
    orderProducer.sendMessage(message);
    return true;
  }


  public OrderResponse confirmOrder(Long orderId, OrderStatus newStatus) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

    order.setStatus(newStatus);
    orderRepository.save(order);

    // Send Kafka message if needed
    OrderMessage message = new OrderMessage();
    message.setOperation("CONFIRM_ORDER");
    message.setOrderId(orderId);
    orderProducer.sendMessage(message);

    return mapToOrderResponse(order);
  }


  public OrderResponse shipOrder(Long orderId, OrderStatus newStatus) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

    order.setStatus(newStatus);
    orderRepository.save(order);

    // Send Kafka message if needed
    OrderMessage message = new OrderMessage();
    message.setOperation("SHIP_ORDER");
    message.setOrderId(orderId);
    orderProducer.sendMessage(message);

    return mapToOrderResponse(order);
  }


  public OrderResponse deliverOrder(Long orderId, OrderStatus newStatus) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

    order.setStatus(newStatus);
    orderRepository.save(order);

    // Send Kafka message if needed
    OrderMessage message = new OrderMessage();
    message.setOperation("DELIVER_ORDER");
    message.setOrderId(orderId);
    orderProducer.sendMessage(message);

    return mapToOrderResponse(order);
  }



  private OrderResponse mapToOrderResponse(Order order) {
    // You need to define this mapper.
    OrderResponse response = new OrderResponse();
    response.setId(order.getId());
    response.setStatus(order.getStatus());
    response.setTotalAmount(order.getTotalAmount());
    response.setPaymentMethod(order.getPaymentMethod());
    response.setCreatedAt(order.getCreatedAt());

    AddressDto addressDto=new AddressDto();
    addressDto.setDoorNumber(order.getDoorNumber());
    addressDto.setAddressLine1(order.getAddressLine1());
    addressDto.setAddressLine2(order.getAddressLine2());
    addressDto.setPinCode(order.getPinCode());
    addressDto.setCity(order.getCity());
    response.setAddressDto(addressDto);
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