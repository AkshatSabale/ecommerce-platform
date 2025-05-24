package com.ecommerce.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "product_reviews", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"product_id", "user_id"})
})
@Getter
@Setter
public class ProductReview {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  private Product product;

  @ManyToOne(fetch = FetchType.LAZY)
  private User user;

  private int rating;

  @Column(columnDefinition = "TEXT")
  private String comment;


  private LocalDateTime createdAt = LocalDateTime.now();
  private LocalDateTime updatedAt = LocalDateTime.now();
}
