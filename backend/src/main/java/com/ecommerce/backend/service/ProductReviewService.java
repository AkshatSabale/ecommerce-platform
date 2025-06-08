package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.ProductReviewRequest;
import com.ecommerce.backend.dto.ProductReviewResponse;
import com.ecommerce.backend.kafka.ProductReviewMessage;
import com.ecommerce.backend.kafka.ProductReviewPayload;
import com.ecommerce.backend.kafka.ProductReviewProducer;
import com.ecommerce.backend.model.Product;
import com.ecommerce.backend.model.ProductReview;
import com.ecommerce.backend.model.User;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.repository.ProductReviewRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.*;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class ProductReviewService {

  private final ProductReviewRepository reviewRepository;
  private final ProductReviewProducer reviewProducer;

  @CacheEvict(value = {"productReviews", "productAverageRatings"}, key = "#request.productId")
  public ProductReviewResponse addOrUpdateReview(Long userId, ProductReviewRequest request) {
    // First try to find existing review
    Optional<ProductReview> existingReview = reviewRepository.findByProductIdAndUserId(
        request.getProductId(),
        userId
    );

    ProductReviewMessage message = new ProductReviewMessage();
    message.setOperation("CREATE_OR_UPDATE");
    message.setProductId(request.getProductId());
    message.setUserId(userId);

    // If review exists, include its ID for update
    existingReview.ifPresent(review -> message.setReviewId(review.getId()));

    ProductReviewPayload payload = new ProductReviewPayload(
        request.getProductId(),
        userId,
        request.getRating(),
        request.getComment()
    );
    message.setReviewPayload(payload);

    reviewProducer.sendMessage(message);

    return existingReview.map(this::mapToResponse)
        .orElseGet(ProductReviewResponse::new);
  }


  @CacheEvict(value = {"productReviews", "productAverageRatings"}, key = "#productId")
  public void deleteReview(Long userId, Long productId) {
    ProductReviewMessage message = new ProductReviewMessage();
    message.setOperation("DELETE");
    message.setProductId(productId);
    message.setUserId(userId);
    reviewProducer.sendMessage(message);
  }

  @Cacheable(value = "productReviews", key = "#productId")
  public List<ProductReviewResponse> getReviewsForProduct(Long productId) {
    return reviewRepository.findAllByProductId(productId).stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  @Cacheable(value = "productAverageRatings", key = "#productId")
  public Double getAverageRatingForProduct(Long productId) {
    Double avg = reviewRepository.findAverageRatingForProduct(productId);
    return avg != null ? avg : 0.0;
  }



  private ProductReviewResponse mapToResponse(ProductReview review) {
    return new ProductReviewResponse(
            review.getUser().getUsername(),
        review.getRating(),
            review.getComment(),
            review.getCreatedAt(),
            review.getUpdatedAt(),
        Boolean.TRUE.equals(review.getVerifiedPurchase())
        );
  }
}