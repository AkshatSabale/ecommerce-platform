package com.ecommerce.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressResponse {

  private String doorNumber;

  private String addressLine1;

  private String addressLine2;

  private Long pinCode;

  private String City;
}
