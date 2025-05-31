package com.ecommerce.backend.kafka;

import com.ecommerce.backend.dto.AddressResponse;
import com.ecommerce.backend.model.Product;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressMessage {
  private String operation; // CREATE, UPDATE, DELETE
  private AddressResponse addressResponse;
  private Long userId; // Needed for DELETE

}
