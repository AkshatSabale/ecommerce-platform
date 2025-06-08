package com.ecommerce.backend.kafka;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductReviewMessage {
  private String operation; // CREATE, UPDATE, DELETE
  private Long reviewId; // For UPDATE operations
  private ProductReviewPayload reviewPayload;
  private Long productId;
  private Long userId;
}