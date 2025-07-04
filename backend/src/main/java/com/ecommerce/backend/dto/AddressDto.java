package com.ecommerce.backend.dto;

import java.io.Serializable;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Data
@Getter
@Setter
@NoArgsConstructor
public class AddressDto implements Serializable
{

  private String doorNumber;
  private String addressLine1;
  private String addressLine2;
  private Long pinCode;
  private String City;
}
