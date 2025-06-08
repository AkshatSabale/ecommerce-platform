package com.ecommerce.backend.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class PaymentResponse {
  private Long id;
  private String paymentId;
  private Double amount;
  private String currency;
  private String status;
  private String createdAt;
  private Long orderId; // nullable
}