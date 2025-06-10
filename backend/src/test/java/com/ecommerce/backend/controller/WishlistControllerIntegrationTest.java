package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.AddToWishlistRequest;
import com.ecommerce.backend.model.Product;

import com.ecommerce.backend.model.Wishlist;
import com.ecommerce.backend.repository.ProductRepository;

import com.ecommerce.backend.repository.WishlistRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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

class WishlistControllerIntegrationTest extends BaseIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private WishlistRepository wishlistRepository;

  @Autowired
  private ProductRepository productRepository;

  @MockBean
  private CacheManager cacheManager;

  private static final Logger logger = LoggerFactory.getLogger(WishlistControllerIntegrationTest.class);

  @BeforeEach
  void setUp() {
    logger.warn("Running WishlistControllerIntegrationTest setUp 🚀");

    Cache mockCache = new ConcurrentMapCache("wishlists");
    Cache userCache = new ConcurrentMapCache("user");

    when(cacheManager.getCache("wishlists")).thenReturn(mockCache);
    when(cacheManager.getCache("user")).thenReturn(userCache);

    wishlistRepository.deleteAllInBatch();
    productRepository.deleteAllInBatch();

    // Add some products
    Product p1 = new Product();
    p1.setName("Wishlist Product 1");
    p1.setPrice(50.0);
    p1.setQuantity(100L);
    p1.setDescription("Wishlist Desc 1");
    p1.setImageFilename("wishlist1.png");

    productRepository.save(p1);

    logger.warn("WishlistControllerIntegrationTest setUp complete ✅");
  }

  @AfterEach
  void cleanup() {
    logger.warn("Cleaning up WishlistControllerIntegrationTest ✅");
    wishlistRepository.deleteAllInBatch();
    productRepository.deleteAllInBatch();
  }

  @Test
  void testGetWishlistInitiallyEmpty() throws Exception {
    logger.warn("Running testGetWishlistInitiallyEmpty 🚀");

    mockMvc.perform(get("/wishlist")
            .with(SecurityMockMvcRequestPostProcessors.user(TEST_USERNAME))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.productIds", hasSize(0)));

    logger.warn("testGetWishlistInitiallyEmpty passed ✅");
  }

  @Test
  @WithMockUser(username = TEST_USERNAME)
  void testAddToWishlist() throws Exception {
    logger.warn("Running testAddToWishlist 🚀");

    Product p = productRepository.findAll().get(0);

    AddToWishlistRequest req = new AddToWishlistRequest();
    req.setProductId(p.getId());

    mockMvc.perform(post("/wishlist/items")
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(req)))
        .andExpect(status().isOk());

    logger.warn("testAddToWishlist passed ✅");
  }

  @Test
  @WithMockUser(username = TEST_USERNAME)
  void testRemoveFromWishlist() throws Exception {
    logger.warn("Running testRemoveFromWishlist 🚀");

    // First add a product
    Product p = productRepository.findAll().get(0);

    Wishlist w = new Wishlist();
    w.setUserId(userRepository.findByUsername(TEST_USERNAME).get().getId());
    w.setProductIds(new HashSet<>(Collections.singletonList(p.getId())));
    wishlistRepository.save(w);

    // Now remove it
    mockMvc.perform(delete("/wishlist/items/" + p.getId())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    logger.warn("testRemoveFromWishlist passed ✅");
  }

  @Test
  @WithMockUser(username = TEST_USERNAME)
  void testClearWishlist() throws Exception {
    logger.warn("Running testClearWishlist 🚀");

    // First add a product
    Product p = productRepository.findAll().get(0);

    Wishlist w = new Wishlist();
    w.setUserId(userRepository.findByUsername(TEST_USERNAME).get().getId());
    w.setProductIds(new HashSet<>(Collections.singletonList(p.getId())));
    wishlistRepository.save(w);

    // Clear the wishlist
    mockMvc.perform(delete("/wishlist")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());



    logger.warn("testClearWishlist passed ✅");
  }
}

