package com.ecommerce.backend.dto;

import lombok.Data;

@Data
public class AddToCartRequest {
  private Long productId;
  private Long quantity;
}
