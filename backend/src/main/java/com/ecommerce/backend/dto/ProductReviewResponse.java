package com.ecommerce.backend.dto;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductReviewResponse implements Serializable {
  private String username;
  private int rating;
  private String comment;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private boolean verifiedPurchase;
}
