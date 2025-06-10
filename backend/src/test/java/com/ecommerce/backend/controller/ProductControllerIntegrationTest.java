package com.ecommerce.backend.controller;

import com.ecommerce.backend.model.Product;
import com.ecommerce.backend.repository.CartRepository;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.repository.ProductReviewRepository;
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
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
class ProductControllerIntegrationTest extends BaseIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ProductRepository productRepository;

  @Autowired
  private CartRepository cartRepository;

  @Autowired
  private ProductReviewRepository productReviewRepository;

  @MockBean
  private CacheManager cacheManager;

  private static final Logger logger = LoggerFactory.getLogger(ProductControllerIntegrationTest.class);

  @BeforeEach
  void setUp() {
    logger.warn("Running ProductControllerIntegrationTest setUp 🚀");

    Cache mockCache = new ConcurrentMapCache("products");
    when(cacheManager.getCache("products")).thenReturn(mockCache);

    cartRepository.deleteAllInBatch();
    productReviewRepository.deleteAllInBatch();
    productRepository.deleteAllInBatch();

    Product p1 = new Product();
    p1.setName("Integration Product 1");
    p1.setPrice(123.45);
    p1.setQuantity(10L);
    p1.setDescription("Desc 1");
    p1.setImageFilename("img1.png");

    productRepository.save(p1);

    logger.warn("ProductControllerIntegrationTest setUp complete ✅");
  }

  @AfterEach
  void cleanup() {
    logger.warn("Cleaning up ProductControllerIntegrationTest ✅");
    cartRepository.deleteAllInBatch();
    productReviewRepository.deleteAllInBatch();
    productRepository.deleteAllInBatch();
  }

  @Test
  void testGetAllProducts() throws Exception {
    logger.warn("Running testGetAllProducts 🚀");

    mockMvc.perform(get("/api/products")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
        .andExpect(jsonPath("$[0].name", is("Integration Product 1")));

    logger.warn("testGetAllProducts passed ✅");
  }

  @Test
  void testGetProductById() throws Exception {
    logger.warn("Running testGetProductById 🚀");

    Product p = productRepository.findAll().get(0);

    mockMvc.perform(get("/api/products/" + p.getId())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name", is("Integration Product 1")))
        .andExpect(jsonPath("$.price", is(123.45)))
        .andExpect(jsonPath("$.quantity", is(10)));

    logger.warn("testGetProductById passed ✅");
  }

  @Test
  void testGetProductById_NotFound() throws Exception {
    logger.warn("Running testGetProductById_NotFound 🚀");

    mockMvc.perform(get("/api/products/99999")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());

    logger.warn("testGetProductById_NotFound passed ✅");
  }
}

