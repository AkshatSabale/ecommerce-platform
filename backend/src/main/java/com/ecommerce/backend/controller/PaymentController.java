package com.ecommerce.backend.controller;

import com.ecommerce.backend.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payment")
public class PaymentController {

  private final PaymentService paymentService;

  @PostMapping("/create")
  public ResponseEntity<?> createOrder(@RequestParam Double amount) {
    try {
      String razorpayOrder = paymentService.createPaymentOrder(amount, "INR");
      return ResponseEntity.ok(razorpayOrder);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Payment order creation failed");
    }
  }


  @GetMapping("/details/{paymentId}")
  public ResponseEntity<?> getPaymentDetails(@PathVariable String paymentId) {
    try {
      String details = paymentService.getPaymentDetails(paymentId);
      return ResponseEntity.ok(details);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Failed to fetch payment details");
    }
  }

  @PostMapping("/refund")
  public ResponseEntity<?> refundPayment(
      @RequestParam String paymentId,
      @RequestParam int amount) {
    try {
      String refund = paymentService.refundPayment(paymentId, amount);
      return ResponseEntity.ok(refund);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Refund failed");
    }
  }

  @PostMapping("/verify")
  public ResponseEntity<Boolean> verifySignature(
      @RequestParam String orderId,
      @RequestParam String paymentId,
      @RequestParam String signature) {
    boolean verified = paymentService.verifySignature(orderId, paymentId, signature);
    return ResponseEntity.ok(verified);
  }


}
