package com.ecommerce.backend.dto;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemResponse implements Serializable
{
  private Long id;
  private Long productId;
  private Integer quantity;
  private Double price;       // price per unit when order was placed
  private Double totalPrice;
}
