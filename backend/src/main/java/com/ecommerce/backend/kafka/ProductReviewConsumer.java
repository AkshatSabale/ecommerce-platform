package com.ecommerce.backend.kafka;

import com.ecommerce.backend.model.Product;
import com.ecommerce.backend.model.ProductReview;
import com.ecommerce.backend.model.User;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.repository.ProductReviewRepository;
import com.ecommerce.backend.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ProductReviewConsumer {

  @Autowired
  private ProductReviewRepository reviewRepository;

  @Autowired
  private ProductRepository productRepository;

  @Autowired
  private UserService userService;

  @Autowired
  private CacheManager cacheManager; // For cache eviction

  @KafkaListener(topics = "product-review-topic", groupId = "product_review_group")
  public void consume(String messageJson) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      ProductReviewMessage message = mapper.readValue(messageJson, ProductReviewMessage.class);

      if ("CREATE_OR_UPDATE".equals(message.getOperation())) {
        handleCreateOrUpdateReview(message);
      } else if ("DELETE".equals(message.getOperation())) {
        handleDeleteReview(message);
      } else {
        log.error("Unknown operation: " + message.getOperation());
      }
    } catch (Exception e) {
      log.error("Error processing review message", e);
    }
  }

  private void handleCreateOrUpdateReview(ProductReviewMessage message) {
    ProductReviewPayload payload = message.getReviewPayload();
    Product product = productRepository.findById(payload.getProductId())
        .orElseThrow(() -> new RuntimeException("Product not found"));

    User user = userService.getUserById(payload.getUserId());

    // Try to find existing review first
    ProductReview review = reviewRepository.findByProductIdAndUserId(product.getId(), user.getId())
        .orElse(new ProductReview());

    // Update review fields
    review.setProduct(product);
    review.setUser(user);
    review.setRating(payload.getRating());
    review.setComment(payload.getComment());
    review.setUpdatedAt(LocalDateTime.now());

    // Set created at only for new reviews
    if (review.getId() == null) {
      review.setCreatedAt(LocalDateTime.now());
    }

    // Check if verified purchase
    if (user.getProductsPurchased().contains(product.getId())) {
      review.setVerifiedPurchase(true);
    }

    reviewRepository.save(review);
    evictReviewCaches(product.getId());
  }

  private void handleDeleteReview(ProductReviewMessage message) {
    ProductReview review = reviewRepository.findByProductIdAndUserId(
        message.getProductId(),
        message.getUserId()
    ).orElseThrow(() -> new RuntimeException("Review not found"));

    reviewRepository.delete(review);
    evictReviewCaches(message.getProductId());
  }

  private void evictReviewCaches(Long productId) {
    Objects.requireNonNull(cacheManager.getCache("productReviews")).evict(productId);
    Objects.requireNonNull(cacheManager.getCache("productAverageRatings")).evict(productId);
  }
}