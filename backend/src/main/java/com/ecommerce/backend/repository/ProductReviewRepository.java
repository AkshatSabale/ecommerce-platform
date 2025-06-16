package com.ecommerce.backend.repository;

import com.ecommerce.backend.model.ProductReview;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {
  Optional<ProductReview> findByProductIdAndUserId(Long productId, Long userId);

  List<ProductReview> findAllByProductId(Long productId);

  @Query("SELECT AVG(r.rating) FROM ProductReview r WHERE r.product.id = :productId")
  Double findAverageRatingForProduct(@Param("productId") Long productId);
}
