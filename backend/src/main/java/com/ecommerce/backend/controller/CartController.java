package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.AddToCartRequest;
import com.ecommerce.backend.dto.CartResponse;
import com.ecommerce.backend.kafka.CartMessage;
import com.ecommerce.backend.kafka.CartProducer;
import com.ecommerce.backend.model.User;
import com.ecommerce.backend.service.CartService;
import com.ecommerce.backend.service.UserService;
import com.ecommerce.backend.util.AuthUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@Slf4j
public class CartController {

  private final CartService cartService;
  private final UserService userService;
  private final AuthUtil authUtil;

  @GetMapping
  public ResponseEntity<CartResponse> getCart() {
    Long userId = authUtil.getAuthenticatedUserId();
    return ResponseEntity.ok(cartService.getCart(userId));
  }

  @PostMapping("/items")
  public ResponseEntity<Void> addToCart(@RequestBody @Valid AddToCartRequest request) {
    Long userId = authUtil.getAuthenticatedUserId();
    cartService.addToCart(userId, request.getProductId(), request.getQuantity());
    return ResponseEntity.accepted().build();
  }

  @PutMapping("/items/{productId}")
  public ResponseEntity<Void> updateCartItem(@PathVariable Long productId, @RequestParam int quantity) {
    Long userId = authUtil.getAuthenticatedUserId();
    cartService.updateCartItem(userId, productId, quantity);
    return ResponseEntity.accepted().build();
  }

  @DeleteMapping("/items/{productId}")
  public ResponseEntity<Void> removeFromCart(@PathVariable Long productId) {
    Long userId = authUtil.getAuthenticatedUserId();
    cartService.removeFromCart(userId, productId);
    return ResponseEntity.accepted().build();
  }

  @DeleteMapping
  public ResponseEntity<Void> clearCart() {
    Long userId = authUtil.getAuthenticatedUserId();
    cartService.clearCart(userId);
    return ResponseEntity.accepted().build();
  }


}