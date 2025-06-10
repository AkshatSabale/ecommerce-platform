package com.ecommerce.backend.controller;


import com.ecommerce.backend.dto.AddToCartRequest;
import com.ecommerce.backend.model.Cart;
import com.ecommerce.backend.model.CartItem;
import com.ecommerce.backend.model.Product;
import com.ecommerce.backend.model.Role;
import com.ecommerce.backend.model.User;
import com.ecommerce.backend.repository.CartRepository;
import com.ecommerce.backend.repository.ProductRepository;


import com.ecommerce.backend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
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
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.security.test.context.support.WithMockUser;

import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CartControllerIntegrationTest extends BaseIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private CartRepository cartRepository;

  @Autowired
  private ProductRepository productRepository;

  @Autowired
  private UserRepository userRepository;

  @MockBean
  private CacheManager cacheManager;

  @Autowired
  private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

  private static final Logger logger = LoggerFactory.getLogger(CartControllerIntegrationTest.class);

  @BeforeEach
  void setUp() {
    logger.warn("Running CartControllerIntegrationTest setUp 🚀");

    // Insert user for TEST_USERNAME if not present
    if (userRepository.findByUsername(TEST_USERNAME).isEmpty()) {
      User user = new User();
      user.setUsername(TEST_USERNAME);
      user.setPassword("dummy");
      user.setEmail("dummy@example.com");
      com.ecommerce.backend.model.Role customerRole = new Role();
      customerRole.setName("ROLE_CUSTOMER");
      roleRepository.save(customerRole);

      userRepository.save(user);
    }

    // Setup caches
    Cache cartCache = new ConcurrentMapCache("cart");
    Cache userCache = new ConcurrentMapCache("user");

    when(cacheManager.getCache("cart")).thenReturn(cartCache);
    when(cacheManager.getCache("user")).thenReturn(userCache);

    // Cleanup DB in correct order to avoid FK violations
    cartRepository.findAll().forEach(cart -> {
      cart.getItems().clear();
      cartRepository.save(cart);
    });
    cartRepository.flush();
    cartRepository.deleteAllInBatch();
    productRepository.deleteAllInBatch();

    // Insert 1 product
    Product p1 = new Product();
    p1.setName("Cart Product 1");
    p1.setPrice(50.0);
    p1.setQuantity(100L);
    p1.setDescription("Cart Desc 1");
    p1.setImageFilename("cart1.png");

    productRepository.save(p1);

    logger.warn("CartControllerIntegrationTest setUp complete ✅");
  }

  @AfterEach
  void cleanup() {
    logger.warn("Stopping Kafka listeners to prevent FK violations");
    kafkaListenerEndpointRegistry.getListenerContainers()
        .forEach(container -> container.stop());

    logger.warn("Cleaning up CartControllerIntegrationTest ✅");

    cartRepository.findAll().forEach(cart -> {
      cart.getItems().clear();
      cartRepository.save(cart);
    });

    cartRepository.flush();

    cartRepository.deleteAllInBatch();

    productRepository.deleteAllInBatch();
    userRepository.deleteAllInBatch();

    logger.warn("Restarting Kafka listeners");
    kafkaListenerEndpointRegistry.getListenerContainers()
        .forEach(container -> container.start());
  }

  @Test
  @WithMockUser(username = TEST_USERNAME)
  void testAddToCart() throws Exception {
    logger.warn("Running testAddToCart 🚀");

    Product p = productRepository.findAll().get(0);

    AddToCartRequest req = new AddToCartRequest();
    req.setProductId(p.getId());
    req.setQuantity(2);

    mockMvc.perform(post("/cart/items")
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(req)))
        .andExpect(status().isAccepted());

    logger.warn("testAddToCart passed ✅");
  }

  @Test
  @WithMockUser(username = TEST_USERNAME)
  void testUpdateCartItem() throws Exception {
    logger.warn("Running testUpdateCartItem 🚀");

    Product p = productRepository.findAll().get(0);
    User user = userRepository.findByUsername(TEST_USERNAME).get();

    // First add to cart manually through Kafka (simulate consumer)
    Cart cart = new Cart();
    cart.setUser(user);
    cart.setCreatedAt(LocalDateTime.now());
    cart.setUpdatedAt(LocalDateTime.now());

    CartItem item = new CartItem();
    item.setCart(cart);
    item.setProduct(p);
    item.setQuantity(1);

    cart.getItems().add(item);
    cartRepository.save(cart);

    // Now update via API
    mockMvc.perform(put("/cart/items/" + p.getId())
            .param("quantity", "5")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isAccepted());

    logger.warn("testUpdateCartItem passed ✅");
  }

  @Test
  @WithMockUser(username = TEST_USERNAME)
  void testRemoveFromCart() throws Exception {
    logger.warn("Running testRemoveFromCart 🚀");

    Product p = productRepository.findAll().get(0);
    User user = userRepository.findByUsername(TEST_USERNAME).get();

    // Add to cart manually through Kafka (simulate consumer)
    Cart cart = new Cart();
    cart.setUser(user);
    cart.setCreatedAt(LocalDateTime.now());
    cart.setUpdatedAt(LocalDateTime.now());

    CartItem item = new CartItem();
    item.setCart(cart);
    item.setProduct(p);
    item.setQuantity(1);

    cart.getItems().add(item);
    cartRepository.save(cart);

    // Now remove via API
    mockMvc.perform(delete("/cart/items/" + p.getId())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isAccepted());

    logger.warn("testRemoveFromCart passed ✅");
  }

  @Test
  @WithMockUser(username = TEST_USERNAME)
  void testClearCart() throws Exception {
    logger.warn("Running testClearCart 🚀");

    Product p = productRepository.findAll().get(0);
    User user = userRepository.findByUsername(TEST_USERNAME).get();

    // Add to cart manually through Kafka (simulate consumer)
    Cart cart = new Cart();
    cart.setUser(user);
    cart.setCreatedAt(LocalDateTime.now());
    cart.setUpdatedAt(LocalDateTime.now());

    CartItem item = new CartItem();
    item.setCart(cart);
    item.setProduct(p);
    item.setQuantity(1);

    cart.getItems().add(item);
    cartRepository.save(cart);

    // Now clear via API
    mockMvc.perform(delete("/cart")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isAccepted());

    logger.warn("testClearCart passed ✅");
  }

  @Test
  @WithMockUser(username = TEST_USERNAME)
  void testGetCartInitiallyEmpty() throws Exception {
    logger.warn("Running testGetCartInitiallyEmpty 🚀");

    mockMvc.perform(get("/cart")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items", hasSize(0)));

    logger.warn("testGetCartInitiallyEmpty passed ✅");
  }
}
