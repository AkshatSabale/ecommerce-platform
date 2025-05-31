package com.ecommerce.backend.kafka;

import lombok.Getter;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WishlistMessage {
  private String operation;  // ADD, REMOVE, CLEAR
  private Long userId;
  private Long productId;    // for ADD or REMOVE
}
