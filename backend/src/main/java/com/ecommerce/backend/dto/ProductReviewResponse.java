package com.ecommerce.backend.dto;
import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductReviewResponse {
  private Long id;
  private Long productId;
  private String username;
  private int rating;
  private String comment;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
