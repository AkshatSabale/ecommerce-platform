package com.ecommerce.backend.controller;


import com.ecommerce.backend.dto.CartResponse;
import com.ecommerce.backend.dto.OrderResponse;
import com.ecommerce.backend.dto.ReturnRequestDto;
import com.ecommerce.backend.model.User;
import com.ecommerce.backend.service.OrderService;
import com.ecommerce.backend.service.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
@Slf4j
public class OrderController
{

  private final UserService userService;
  private final OrderService orderService;

  @GetMapping
  public ResponseEntity<List<OrderResponse>> getCart() {
    Long userId = getAuthenticatedUserId();
    return ResponseEntity.ok(orderService.getOrder(userId));
  }

  @GetMapping("/{orderId}")
  public ResponseEntity<OrderResponse> getCartById(
      @PathVariable Long orderId )
  {
    Long userId = getAuthenticatedUserId();
    return ResponseEntity.ok(orderService.getOrderById(userId,orderId));
  }

  @PatchMapping("/{orderId}/cancel")
  public ResponseEntity<String> cancelOrder(
    @PathVariable Long orderId )
  {
    Long userId = getAuthenticatedUserId();
    orderService.clearOrder(userId,orderId);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{orderId}/return")
  public ResponseEntity<String> requestReturn(@PathVariable Long orderId, @RequestBody ReturnRequestDto returnRequest) {
    Long userId = getAuthenticatedUserId();
    boolean success = orderService.requestReturn(orderId, returnRequest,userId);
    if (success) {
      return ResponseEntity.ok("Return request submitted.");
    } else {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Unable to process return request.");
    }
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/{orderId}/approve-return")
  public ResponseEntity<String> approveReturn(@PathVariable Long orderId) {
    boolean success = orderService.approveReturn(orderId);
    if (success) {
      return ResponseEntity.ok("Return approved.");
    } else {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Unable to approve return.");
    }
  }

  @PutMapping("/users/{userId}/orders/{orderId}/confirm")
  public ResponseEntity<OrderResponse> confirmOrder(
      @PathVariable Long userId,
      @PathVariable Long orderId) {
    OrderResponse response = orderService.confirmOrderAndDeductInventory(userId, orderId);
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
