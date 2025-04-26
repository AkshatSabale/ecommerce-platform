package com.ecommerce.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartItemDto {
  private Long id;
  private String productName;
  private Long quantity;

  // getters/setters
}
