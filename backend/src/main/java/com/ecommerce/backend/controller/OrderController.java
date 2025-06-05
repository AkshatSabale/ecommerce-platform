package com.ecommerce.backend.controller;


import com.ecommerce.backend.dto.CartResponse;
import com.ecommerce.backend.dto.OrderResponse;
import com.ecommerce.backend.dto.ReturnRequestDto;
import com.ecommerce.backend.model.Order;
import com.ecommerce.backend.model.OrderStatus;
import com.ecommerce.backend.model.User;
import com.ecommerce.backend.service.OrderService;
import com.ecommerce.backend.service.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

  private final UserService userService;
  private final OrderService orderService;

  @GetMapping
  public ResponseEntity<List<OrderResponse>> getCart() {
    Long userId = getAuthenticatedUserId();
    return ResponseEntity.ok(orderService.getOrder(userId));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/admin/orders")
  public ResponseEntity<List<OrderResponse>> getAllOrders(
      @RequestParam(required = false) OrderStatus status,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    Pageable pageable = PageRequest.of(page, size);
    List<OrderResponse> orders = orderService.getAllOrders(status, pageable);
    return ResponseEntity.ok(orders);
  }

  @GetMapping("/{orderId}")
  public ResponseEntity<OrderResponse> getCartById(@PathVariable Long orderId) {
    Long userId = getAuthenticatedUserId();
    return ResponseEntity.ok(orderService.getOrderById(userId, orderId));
  }

  @PatchMapping("/{orderId}/cancel")
  public ResponseEntity<String> cancelOrder(@PathVariable Long orderId) {
    Long userId = getAuthenticatedUserId();
    Order order = orderService.getOrderEntityById(userId, orderId);

    // Validate order can be cancelled
    if (!order.getStatus().equals(OrderStatus.PENDING) &&
        !order.getStatus().equals(OrderStatus.CONFIRMED) &&
        !order.getStatus().equals(OrderStatus.SHIPPED)) {
      throw new IllegalStateException("Order cannot be cancelled in its current state");
    }

    orderService.clearOrder(userId, orderId);
    return ResponseEntity.accepted().body("Order cancellation request submitted.");
  }

  @PostMapping("/{orderId}/return")
  public ResponseEntity<String> requestReturn(@PathVariable Long orderId,
      @RequestBody ReturnRequestDto returnRequest) {
    Long userId = getAuthenticatedUserId();
    Order order = orderService.getOrderEntityById(userId, orderId);

    // Validate order can be returned
    if (!order.getStatus().equals(OrderStatus.DELIVERED)) {
      throw new IllegalStateException("Only delivered orders can be returned");
    }

    orderService.requestReturn(orderId, returnRequest, userId);
    return ResponseEntity.accepted().body("Return request submitted.");
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/{orderId}/complete-return")
  public ResponseEntity<String> completeReturn(@PathVariable Long orderId) {
    Order order = orderService.getOrderEntityById(orderId);

    if (!order.getStatus().equals(OrderStatus.RETURN_APPROVED)) {
      throw new IllegalStateException("Only approved returns can be completed");
    }

    orderService.completeReturn(orderId);
    return ResponseEntity.accepted().body("Return completed successfully.");
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/{orderId}/approve-return")
  public ResponseEntity<String> approveReturn(@PathVariable Long orderId) {
    orderService.approveReturn(orderId);
    return ResponseEntity.accepted().body("Return approval request submitted.");
  }


  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/{orderId}/confirm")
  public ResponseEntity<OrderResponse> confirmOrder(@PathVariable Long orderId) {
    Order order = orderService.getOrderEntityById(orderId);

    if (!order.getStatus().equals(OrderStatus.PENDING)) {
      throw new IllegalStateException("Only PENDING orders can be confirmed");
    }

    OrderResponse response = orderService.confirmOrder(orderId, OrderStatus.CONFIRMED);
    return ResponseEntity.ok(response);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/{orderId}/ship")
  public ResponseEntity<OrderResponse> shipOrder(@PathVariable Long orderId) {
    Order order = orderService.getOrderEntityById(orderId);

    if (!order.getStatus().equals(OrderStatus.CONFIRMED)) {
      throw new IllegalStateException("Only CONFIRMED orders can be shipped");
    }

    OrderResponse response = orderService.shipOrder(orderId, OrderStatus.SHIPPED);
    return ResponseEntity.ok(response);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/{orderId}/deliver")
  public ResponseEntity<OrderResponse> deliverOrder(@PathVariable Long orderId) {
    Order order = orderService.getOrderEntityById(orderId);

    if (!order.getStatus().equals(OrderStatus.SHIPPED)) {
      throw new IllegalStateException("Only SHIPPED orders can be delivered");
    }

    OrderResponse response = orderService.deliverOrder(orderId, OrderStatus.DELIVERED);
    return ResponseEntity.ok(response);
  }



  private Long getAuthenticatedUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      throw new SecurityException("User not authenticated");
    }

    Object principal = authentication.getPrincipal();
    if (!(principal instanceof UserDetails)) {
      throw new SecurityException("Invalid authentication principal");
    }

    String username = ((UserDetails) principal).getUsername();
    User user = userService.getUserByUserName(username);

    return user.getId();
  }
}
