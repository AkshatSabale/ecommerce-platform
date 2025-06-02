package com.ecommerce.backend.kafka;

import com.ecommerce.backend.model.Product;
import com.ecommerce.backend.model.ProductReview;
import com.ecommerce.backend.model.User;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.repository.ProductReviewRepository;
import com.ecommerce.backend.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
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

      switch (message.getOperation()) {
        case "CREATE_OR_UPDATE":
          ProductReviewPayload payload = message.getReviewPayload();

          Product product = productRepository.findById(payload.getProductId())
              .orElseThrow(() -> new RuntimeException("Product not found"));

          User user = userService.getUserById(payload.getUserId());

          ProductReview review = reviewRepository.findByProductIdAndUserId(product.getId(), user.getId())
              .orElse(new ProductReview());

          review.setProduct(product);
          review.setUser(user);
          review.setRating(payload.getRating());
          review.setComment(payload.getComment());
          review.setUpdatedAt(LocalDateTime.now());
          if (review.getCreatedAt() == null) {
            review.setCreatedAt(LocalDateTime.now());
          }

          if(user.getProductsPurchased().contains(product.getId()))
            review.setVerifiedPurchase(true);

          reviewRepository.save(review);

          // Evict cache for this product's reviews and average rating
          evictReviewCaches(product.getId());
          break;

        case "DELETE":
          Long productId = message.getProductId();
          Long userId = message.getUserId();

          ProductReview reviewToDelete = reviewRepository.findByProductIdAndUserId(productId, userId)
              .orElseThrow(() -> new RuntimeException("Review not found"));
          reviewRepository.delete(reviewToDelete);

          // Evict cache for this product's reviews and average rating
          evictReviewCaches(productId);
          break;

        default:
          System.out.println("Unknown operation: " + message.getOperation());
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void evictReviewCaches(Long productId) {
    cacheManager.getCache("productReviews").evict(productId);
    cacheManager.getCache("productAverageRatings").evict(productId);
  }
}