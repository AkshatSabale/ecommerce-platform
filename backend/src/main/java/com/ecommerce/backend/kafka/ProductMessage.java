package com.ecommerce.backend.kafka;

import com.ecommerce.backend.model.Product;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductMessage {
  private String operation; // CREATE, UPDATE, DELETE
  private Product product;
  private Long productId; // Needed for DELETE

}