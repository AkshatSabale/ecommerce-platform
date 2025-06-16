package com.ecommerce.backend.dto;

import com.ecommerce.backend.model.Payment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RazorpayOrderResponseDTO {
  private String id; // Razorpay order ID
  private double amount;
  private String currency;
  private String receipt;
  private String status;

  public RazorpayOrderResponseDTO(Payment payment) {
    this.id = payment.getRazorpayOrderId(); // Razorpay order ID
    this.amount = payment.getAmount();
    this.currency = payment.getCurrency();
    this.receipt = payment.getReceipt();
    this.status = payment.getStatus();
  }

  // getters
}