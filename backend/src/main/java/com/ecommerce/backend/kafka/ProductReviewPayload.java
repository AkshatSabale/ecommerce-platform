package com.ecommerce.backend.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductReviewPayload {
  private Long productId;
  private Long userId;
  private int rating;
  private String comment;
}