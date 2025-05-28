package com.ecommerce.backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TopProductDTO {
  private Long productId;
  private Long totalSold;


  public TopProductDTO(Long productId, Long totalSold) {
    this.productId = productId;
    this.totalSold = totalSold;
  }

  // getters
}