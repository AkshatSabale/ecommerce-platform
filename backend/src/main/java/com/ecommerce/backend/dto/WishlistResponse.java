package com.ecommerce.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter @Setter
@AllArgsConstructor
public class WishlistResponse {
  private Long id;
  private Set<Long> productIds;   // front-end can fetch product details lazily
}