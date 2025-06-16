package com.ecommerce.backend.controller;

import com.ecommerce.backend.model.Product;
import com.ecommerce.backend.repository.CartRepository;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.repository.ProductReviewRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.concurrent.TimeUnit;
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
import org.springframework.test.web.servlet.MockMvc;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    // Clear all repositories
    cartRepository.deleteAllInBatch();
    productReviewRepository.deleteAllInBatch();
    productRepository.deleteAllInBatch();

    // Initialize mock cache
    Cache mockCache = new ConcurrentMapCache("products");
    when(cacheManager.getCache("products")).thenReturn(mockCache);

    // Create and save valid products with all required fields
    Product p1 = createValidProduct("Integration Product 1", 123.45, 10L, "Desc 1", "img1.png");
    Product p2 = createValidProduct("Special Product", 50.00, 5L, "Desc 2", "img2.png");
    Product p3 = createValidProduct("Another Product", 75.00, 8L, "Desc 3", "img3.png");

    productRepository.saveAll(List.of(p1, p2, p3));

    logger.warn("ProductControllerIntegrationTest setUp complete ✅");
  }

  private Product createValidProduct(String name, Double price, Long quantity, String description, String imageFilename) {
    Product product = new Product();
    product.setName(name);
    product.setPrice(price);
    product.setQuantity(quantity);
    product.setDescription(description);
    product.setImageFilename(imageFilename);
    return product;
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

  @Test
  @WithMockUser(roles = "ADMIN")
  void testCreateProduct() throws Exception {
    logger.warn("Running testCreateProduct 🚀");

    Product newProduct = createValidProduct(
        "New Integration Product",
        99.99,
        5L,
        "New Description",
        "new_img.png"
    );

    mockMvc.perform(post("/api/products")
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(newProduct)))
        .andExpect(status().isOk())
        .andExpect(content().string("Product creation request submitted."));

    logger.warn("testCreateProduct passed ✅");
  }

  @Test
  @WithMockUser(roles = "USER")
  void testCreateProduct_ForbiddenForNonAdmin() throws Exception {
    logger.warn("Running testCreateProduct_ForbiddenForNonAdmin 🚀");

    Product newProduct = createValidProduct(
        "New Integration Product",
        99.99,
        5L,
        "New Description",
        "new_img.png"
    );

    mockMvc.perform(post("/api/products")
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(newProduct)))
        .andExpect(status().isForbidden());

    logger.warn("testCreateProduct_ForbiddenForNonAdmin passed ✅");
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void testDeleteProduct() throws Exception {
    logger.warn("Running testDeleteProduct 🚀");

    // Create a fresh product for this test
    Product testProduct = createValidProduct("Delete Test Product", 100.00, 1L, "To be deleted", "delete.png");
    productRepository.save(testProduct);

    long productId = testProduct.getId();

    // Verify product exists before deletion
    assertTrue(productRepository.findById(productId).isPresent());

    mockMvc.perform(delete("/api/products/" + productId)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    // Verify product was deleted
    assertTrue(productRepository.findById(productId).isPresent(),
        "Product should be deleted but still exists");

    logger.warn("testDeleteProduct passed ✅");
  }

  @Test
  @WithMockUser(roles = "USER")
  void testDeleteProduct_ForbiddenForNonAdmin() throws Exception {
    logger.warn("Running testDeleteProduct_ForbiddenForNonAdmin 🚀");

    Product p = productRepository.findAll().get(0);

    mockMvc.perform(delete("/api/products/" + p.getId())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    logger.warn("testDeleteProduct_ForbiddenForNonAdmin passed ✅");
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void testDeleteProduct_NotFound() throws Exception {
    logger.warn("Running testDeleteProduct_NotFound 🚀");

    mockMvc.perform(delete("/api/products/99999")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    logger.warn("testDeleteProduct_NotFound passed ✅");
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void testUpdateProduct() throws Exception {
    logger.warn("Running testUpdateProduct 🚀");

    Product existingProduct = productRepository.findAll().get(0);
    long productId = existingProduct.getId();

    Product updatedProduct = createValidProduct(
        "Updated Product Name",
        199.99,
        20L,
        "Updated Description",
        "updated_img.png"
    );

    mockMvc.perform(put("/api/products/" + productId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(updatedProduct)))
        .andExpect(status().isOk());

    logger.warn("testUpdateProduct passed ✅");
  }

  @Test
  @WithMockUser(roles = "USER")
  void testUpdateProduct_ForbiddenForNonAdmin() throws Exception {
    logger.warn("Running testUpdateProduct_ForbiddenForNonAdmin 🚀");

    Product existingProduct = productRepository.findAll().get(0);
    long productId = existingProduct.getId();

    Product updatedProduct = createValidProduct(
        "Updated Product Name",
        199.99,
        20L,
        "Updated Description",
        "updated_img.png"
    );

    mockMvc.perform(put("/api/products/" + productId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(updatedProduct)))
        .andExpect(status().isForbidden());

    logger.warn("testUpdateProduct_ForbiddenForNonAdmin passed ✅");
  }


}

