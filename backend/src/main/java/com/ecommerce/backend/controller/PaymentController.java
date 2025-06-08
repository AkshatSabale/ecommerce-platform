package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.PaymentResponse;
import com.ecommerce.backend.dto.RazorpayOrderResponseDTO;
import com.ecommerce.backend.model.Payment;
import com.ecommerce.backend.model.User;
import com.ecommerce.backend.service.PaymentService;
import com.ecommerce.backend.service.UserService;
import com.razorpay.RazorpayException;
import java.util.List;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

  private final PaymentService paymentService;
  private final UserService userService;

  @PostMapping
  public ResponseEntity<?> createPaymentOrder(
      @RequestParam Double amount,
      @RequestParam String currency) {
    Long userId = getAuthenticatedUserId();
    try {
      Payment payment = paymentService.createAndSavePaymentOrder(amount, currency, userId);
      return ResponseEntity.ok(new RazorpayOrderResponseDTO(payment));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Payment order creation failed: " + e.getMessage());
    }
  }
  @GetMapping("/{paymentId}")
  public ResponseEntity<?> getPayment(@PathVariable String paymentId) {
    Long userId = getAuthenticatedUserId();
    try {
      Payment payment = paymentService.getPaymentForUser(paymentId, userId);
      return ResponseEntity.ok(payment);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body("Payment not found or access denied");
    }
  }

  @GetMapping("/user")
  public ResponseEntity<Page<PaymentResponse>> getUserPayments(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "5") int size
  ) {
    Long userId = getAuthenticatedUserId();
    Page<PaymentResponse> paymentsPage = paymentService.getUserPayments(userId, page, size);
    return ResponseEntity.ok(paymentsPage);
  }

  @PostMapping("/webhook")
  public ResponseEntity<?> handleWebhook(
      @RequestBody String payload,
      @RequestHeader("X-Razorpay-Signature") String signature) {
    try {
      paymentService.handleWebhookEvent(payload, signature);
      return ResponseEntity.ok().build();
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
  }

  @PostMapping("/verify")
  public ResponseEntity<Boolean> verifyPayment(
      @RequestParam String orderId,
      @RequestParam String paymentId,
      @RequestParam String signature) throws RazorpayException {
    Long userId = getAuthenticatedUserId();
    boolean verified = paymentService.verifyAndCompletePayment(orderId, paymentId, signature, userId);
    return ResponseEntity.ok(verified);
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