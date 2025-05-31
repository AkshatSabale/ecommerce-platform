package com.ecommerce.backend.kafka;

import com.ecommerce.backend.dto.ReturnRequestDto;
import com.ecommerce.backend.model.Order;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderMessage {
  private String operation; // CREATE, UPDATE, DELETE, CANCEL, RETURN_REQUEST, APPROVE_RETURN, CONFIRM
  private Order order;
  private Long orderId;
  private Long userId;
  private ReturnRequestDto returnRequest;
}