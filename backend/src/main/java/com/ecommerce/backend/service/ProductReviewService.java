package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.ProductReviewRequest;
import com.ecommerce.backend.dto.ProductReviewResponse;
import com.ecommerce.backend.model.Product;
import com.ecommerce.backend.model.ProductReview;
import com.ecommerce.backend.model.User;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.repository.ProductReviewRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.*;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class ProductReviewService {

  private final ProductReviewRepository reviewRepository;
  private final ProductRepository productRepository;
  private final UserService userService;

  public ProductReviewResponse addOrUpdateReview(Long userId, ProductReviewRequest request) {
    Product product = productRepository.findById(request.getProductId())
        .orElseThrow(() -> new RuntimeException("Product not found"));
    User user = userService.getUserById(userId);

    ProductReview review = reviewRepository.findByProductIdAndUserId(product.getId(), user.getId())
        .orElse(new ProductReview());

    review.setUser(user);
    review.setProduct(product);
    review.setRating(request.getRating());
    review.setComment(request.getComment());
    review.setUpdatedAt(LocalDateTime.now());
    if (review.getCreatedAt() == null) {
      review.setCreatedAt(LocalDateTime.now());
    }

    ProductReview savedReview = reviewRepository.save(review);
    return mapToResponse(savedReview);
  }

  public void deleteReview(Long userId, Long productId) {
    ProductReview review = reviewRepository.findByProductIdAndUserId(productId, userId)
        .orElseThrow(() -> new RuntimeException("Review not found"));
    reviewRepository.delete(review);
  }

  public List<ProductReviewResponse> getReviewsForProduct(Long productId) {
    return reviewRepository.findAllByProductId(productId).stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  public Double getAverageRatingForProduct(Long productId) {
    return reviewRepository.findAverageRatingForProduct(productId);
  }

  private ProductReviewResponse mapToResponse(ProductReview review) {
    return new ProductReviewResponse(
        review.getId(),
        review.getProduct().getId(),
        review.getUser().getUsername(),
        review.getRating(),
        review.getComment(),
        review.getCreatedAt(),
        review.getUpdatedAt()
    );
  }
}