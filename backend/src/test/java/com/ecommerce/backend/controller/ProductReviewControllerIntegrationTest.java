package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.AddToWishlistRequest;
import com.ecommerce.backend.dto.ProductReviewRequest;
import com.ecommerce.backend.model.Product;

import com.ecommerce.backend.model.ProductReview;
import com.ecommerce.backend.model.Wishlist;
import com.ecommerce.backend.repository.ProductRepository;

import com.ecommerce.backend.repository.ProductReviewRepository;
import com.ecommerce.backend.repository.WishlistRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


class ProductReviewControllerIntegrationTest extends BaseIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ProductRepository productRepository;

  @Autowired
  private ProductReviewRepository productReviewRepository;

  @MockBean
  private CacheManager cacheManager;

  private static final Logger logger = LoggerFactory.getLogger(ProductReviewControllerIntegrationTest.class);

  @BeforeEach
  void setUp() {
    logger.warn("Running ProductReviewControllerIntegrationTest setUp 🚀");

    Cache reviewCache = new ConcurrentMapCache("productReviews");
    Cache avgCache = new ConcurrentMapCache("productAverageRatings");
    Cache userCache = new ConcurrentMapCache("user");

    when(cacheManager.getCache("productReviews")).thenReturn(reviewCache);
    when(cacheManager.getCache("productAverageRatings")).thenReturn(avgCache);
    when(cacheManager.getCache("user")).thenReturn(userCache);

    productReviewRepository.deleteAllInBatch();
    productRepository.deleteAllInBatch();

    Product p1 = new Product();
    p1.setName("Review Product 1");
    p1.setPrice(100.0);
    p1.setQuantity(50L);
    p1.setDescription("Review Desc 1");
    p1.setImageFilename("review1.png");

    productRepository.save(p1);

    logger.warn("ProductReviewControllerIntegrationTest setUp complete ✅");
  }

  @AfterEach
  void cleanup() {
    logger.warn("Cleaning up ProductReviewControllerIntegrationTest ✅");
    productReviewRepository.deleteAllInBatch();
    productRepository.deleteAllInBatch();
  }

  @Test
  @WithMockUser(username = TEST_USERNAME)
  void testAddReview() throws Exception {
    logger.warn("Running testAddReview 🚀");

    Product p = productRepository.findAll().get(0);

    ProductReviewRequest req = new ProductReviewRequest();
    req.setProductId(p.getId());
    req.setRating(5);
    req.setComment("Excellent product!");

    mockMvc.perform(post("/reviews")
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(req)))
        .andExpect(status().isOk());

    logger.warn("testAddReview passed ✅");
  }

  @Test
  @WithMockUser(username = TEST_USERNAME)
  void testDeleteReview() throws Exception {
    logger.warn("Running testDeleteReview 🚀");

    Product p = productRepository.findAll().get(0);

    // Insert a review first manually
    ProductReview review = new ProductReview();
    review.setProduct(p);
    review.setUser(userRepository.findByUsername(TEST_USERNAME).get());
    review.setRating(4);
    review.setComment("Nice product");
    review.setCreatedAt(LocalDateTime.now());
    review.setUpdatedAt(LocalDateTime.now());
    productReviewRepository.save(review);

    // Now delete via API
    mockMvc.perform(delete("/reviews/" + p.getId())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    logger.warn("testDeleteReview passed ✅");
  }

  @Test
  @WithMockUser(username = TEST_USERNAME)
  void testGetReviewsInitiallyEmpty() throws Exception {
    logger.warn("Running testGetReviewsInitiallyEmpty 🚀");

    Product p = productRepository.findAll().get(0);

    mockMvc.perform(get("/reviews/product/" + p.getId())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(0)));

    logger.warn("testGetReviewsInitiallyEmpty passed ✅");
  }
}
