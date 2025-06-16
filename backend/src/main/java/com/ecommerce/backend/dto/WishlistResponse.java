package com.ecommerce.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class WishlistResponse {
  private Long id;
  private Set<Long> productIds; // front-end can fetch product details lazily


}