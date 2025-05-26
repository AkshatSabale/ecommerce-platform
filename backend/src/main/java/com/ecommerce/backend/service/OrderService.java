package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.AddressDto;
import com.ecommerce.backend.dto.OrderItemResponse;
import com.ecommerce.backend.dto.OrderResponse;
import com.ecommerce.backend.dto.ReturnRequestDto;
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
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService
{

  private final OrderRepository orderRepository;
  private final UserRepository userRepository;
  private final CheckoutService checkoutService;
  private final UserService userService;
  private final ProductRepository productRepository;

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

  /*
  private OrderResponse createNewOrder(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    return checkoutService.checkout(userId, PaymentMethod.COD);
  }

   */

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

    log.debug("Authenticated userId: {}, Order's userId: {}", userId, order.getUserId());
    if (!order.getUserId().equals(userId)) {
      throw new UnauthorizedException("You cannot cancel this order.");
    }

    order.setStatus(OrderStatus.CANCELLED); // If using status, otherwise delete
    restockItems(order);
    orderRepository.save(order); // save the updated status
  }

  public boolean requestReturn(Long orderId, ReturnRequestDto returnRequest,Long userId) {
    Optional<Order> optionalOrder = orderRepository.findById(orderId);
    if (optionalOrder.isPresent()) {
      Order order = optionalOrder.get();
      if (order.getStatus() == OrderStatus.DELIVERED && order.getUserId().equals(userId)) {
        order.setStatus(OrderStatus.RETURN_REQUESTED);
        // Save return reason and timestamp if needed
        orderRepository.save(order);
        return true;
      }
    }
    return false;
  }

  public boolean approveReturn(Long orderId) {
    Optional<Order> optionalOrder = orderRepository.findById(orderId);
    if (optionalOrder.isPresent()) {
      Order order = optionalOrder.get();
      if (order.getStatus() == OrderStatus.RETURN_REQUESTED) {
        order.setStatus(OrderStatus.RETURNED);
        orderRepository.save(order);
        restockItems(order);
        // Process refund if applicable
        return true;
      }
    }
    return false;
  }

  public void restockItems(Order order) {
    for (OrderItem item : order.getOrderItems()) {
      Optional<Product> optionalProduct = productRepository.findById(item.getProductId());
      if (optionalProduct.isPresent()) {
        Product product = optionalProduct.get();
        product.setQuantity(product.getQuantity() + item.getQuantity());
        productRepository.save(product);
      }
    }
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

