package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.CheckoutRequest;
import com.ecommerce.backend.dto.OrderResponse;
import com.ecommerce.backend.model.PaymentMethod;
import com.ecommerce.backend.model.User;
import com.ecommerce.backend.service.CartService;
import com.ecommerce.backend.service.CheckoutService;
import com.ecommerce.backend.service.UserService;
import com.ecommerce.backend.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/checkout")
@RequiredArgsConstructor
@Slf4j
public class CheckoutController
{
  private final CheckoutService checkoutService;
  private final UserService userService;
  private final AuthUtil authUtil;

  @PostMapping
  public ResponseEntity<OrderResponse> checkout(@RequestBody CheckoutRequest request)
  {

    Long userId = authUtil.getAuthenticatedUserId();
    System.out.println("Checkout request received: " + request);
    OrderResponse orderResponse = checkoutService.checkout(userId, request);
    return ResponseEntity.ok(orderResponse);
  }


}
