package com.ecommerce.backend.service;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecommerce.backend.dto.ProductReviewRequest;
import com.ecommerce.backend.dto.ProductReviewResponse;
import com.ecommerce.backend.kafka.ProductReviewProducer;
import com.ecommerce.backend.model.ProductReview;
import com.ecommerce.backend.model.User;
import com.ecommerce.backend.repository.ProductReviewRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import org.springframework.test.annotation.Rollback;

@ExtendWith(MockitoExtension.class)
@Rollback
class ProductReviewServiceTest {

  @InjectMocks
  private ProductReviewService reviewService;

  @Mock
  private ProductReviewRepository reviewRepository;

  @Mock
  private ProductReviewProducer reviewProducer;

  private static final Logger logger = LoggerFactory.getLogger(ProductReviewServiceTest.class);

  @BeforeEach
  void setUp() {
    logger.warn("Running ProductReviewServiceTest setUp 🚀");
  }

  @Test
  void testAddOrUpdateReview_WhenReviewDoesNotExist() {
    logger.warn("Running testAddOrUpdateReview_WhenReviewDoesNotExist 🚀");

    Long userId = 100L;
    Long productId = 200L;

    when(reviewRepository.findByProductIdAndUserId(productId, userId)).thenReturn(Optional.empty());

    ProductReviewRequest req = new ProductReviewRequest();
    req.setProductId(productId);
    req.setRating(5);
    req.setComment("Great product!");

    ProductReviewResponse response = reviewService.addOrUpdateReview(userId, req);

    verify(reviewProducer, times(1)).sendMessage(any());
    assertNotNull(response);

    logger.warn("testAddOrUpdateReview_WhenReviewDoesNotExist passed ✅");
  }

  @Test
  void testDeleteReview() {
    logger.warn("Running testDeleteReview 🚀");

    Long userId = 100L;
    Long productId = 200L;

    reviewService.deleteReview(userId, productId);

    verify(reviewProducer, times(1)).sendMessage(any());

    logger.warn("testDeleteReview passed ✅");
  }

  @Test
  void testGetReviewsForProduct() {
    logger.warn("Running testGetReviewsForProduct 🚀");

    Long productId = 200L;

    ProductReview review = new ProductReview();
    review.setId(1L);
    review.setRating(4);
    review.setComment("Nice");
    review.setCreatedAt(LocalDateTime.now());
    review.setUpdatedAt(LocalDateTime.now());

    User user = new User();
    user.setUsername("testuser");

    review.setUser(user);

    when(reviewRepository.findAllByProductId(productId)).thenReturn(Collections.singletonList(review));

    List<ProductReviewResponse> responses = reviewService.getReviewsForProduct(productId);

    assertEquals(1, responses.size());
    assertEquals("testuser", responses.get(0).getUsername());
    assertEquals(4, responses.get(0).getRating());
    assertEquals("Nice", responses.get(0).getComment());

    logger.warn("testGetReviewsForProduct passed ✅");
  }

  @Test
  void testGetAverageRatingForProduct_WhenExists() {
    logger.warn("Running testGetAverageRatingForProduct_WhenExists 🚀");

    Long productId = 200L;

    when(reviewRepository.findAverageRatingForProduct(productId)).thenReturn(4.5);

    Double avg = reviewService.getAverageRatingForProduct(productId);

    assertEquals(4.5, avg);

    logger.warn("testGetAverageRatingForProduct_WhenExists passed ✅");
  }

  @Test
  void testGetAverageRatingForProduct_WhenNull() {
    logger.warn("Running testGetAverageRatingForProduct_WhenNull 🚀");

    Long productId = 200L;

    when(reviewRepository.findAverageRatingForProduct(productId)).thenReturn(null);

    Double avg = reviewService.getAverageRatingForProduct(productId);

    assertEquals(0.0, avg);

    logger.warn("testGetAverageRatingForProduct_WhenNull passed ✅");
  }
}
