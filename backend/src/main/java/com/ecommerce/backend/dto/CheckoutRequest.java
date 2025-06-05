package com.ecommerce.backend.dto;

import com.ecommerce.backend.model.PaymentMethod;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class CheckoutRequest {
  private PaymentMethod paymentMethod;
  private AddressDto address;
  private String paymentId;
  private String orderId;

}
