package com.ecommerce.backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductReviewRequest {
  private Long productId;
  private int rating;
  private String comment;
}