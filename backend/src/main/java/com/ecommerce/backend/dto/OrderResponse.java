package com.ecommerce.backend.dto;

import com.ecommerce.backend.model.OrderStatus;
import com.ecommerce.backend.model.PaymentMethod;
import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse implements Serializable
{
  private Long id;
  private List<OrderItemResponse> list;
  private OrderStatus status;
  private Double totalAmount;
  private PaymentMethod paymentMethod;
  private AddressDto addressDto;
}
