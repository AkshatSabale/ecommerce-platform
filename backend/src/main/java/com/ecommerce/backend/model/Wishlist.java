package com.ecommerce.backend.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "wishlists")
@Getter
@Setter
public class Wishlist {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long userId;

  /** just keep product IDs; no need for a join entity yet */
  @ElementCollection
  @CollectionTable(name = "wishlist_products",
      joinColumns = @JoinColumn(name = "wishlist_id"))
  @Column(name = "product_id")
  private Set<Long> productIds = new HashSet<>();
}
