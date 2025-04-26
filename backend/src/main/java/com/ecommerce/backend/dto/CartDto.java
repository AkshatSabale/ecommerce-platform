package com.ecommerce.backend.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartDto {
  private Long id;
  private String userEmail;
  private LocalDateTime updatedAt;
  private List<CartItemDto> items;

  // getters/setters
}
