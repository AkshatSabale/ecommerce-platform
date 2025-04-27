package com.ecommerce.backend.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItemDto {
  private Long productId;
  private String productName;
  private Double productPrice;
  private int quantity;
}
