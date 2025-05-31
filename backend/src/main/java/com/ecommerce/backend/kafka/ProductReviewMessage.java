package com.ecommerce.backend.kafka;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductReviewMessage {
  private String operation; // CREATE_OR_UPDATE, DELETE
  private ProductReviewPayload reviewPayload;
  private Long productId; // for DELETE
  private Long userId; // for DELETE
}