package com.ecommerce.backend.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartMessage {
  private Long userId;
  private String operation; // ADD, UPDATE, DELETE, CLEAR
  private Long productId;
  private Integer quantity;
}