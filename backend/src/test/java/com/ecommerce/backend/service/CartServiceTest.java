package com.ecommerce.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


import com.ecommerce.backend.dto.CartResponse;
import com.ecommerce.backend.kafka.CartProducer;
import com.ecommerce.backend.model.Cart;
import com.ecommerce.backend.model.User;
import com.ecommerce.backend.repository.CartRepository;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.repository.ProductReviewRepository;
import com.ecommerce.backend.repository.UserRepository;
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
class CartServiceTest {

  @InjectMocks
  private CartService cartService;

  @Mock
  private CartRepository cartRepository;

  @Mock
  private ProductRepository productRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private CartProducer cartProducer;

  private static final Logger logger = LoggerFactory.getLogger(CartServiceTest.class);

  @BeforeEach
  void setUp() {
    logger.warn("Running CartServiceTest setUp 🚀");
  }

  @Test
  void testGetCart_WhenCartExists() {
    logger.warn("Running testGetCart_WhenCartExists 🚀");

    Long userId = 100L;

    Cart cart = new Cart();
    cart.setId(1L);
    cart.setUser(new User());
    cart.setItems(Collections.emptyList());
    cart.setUpdatedAt(LocalDateTime.now());

    when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));

    CartResponse response = cartService.getCart(userId);

    assertNotNull(response);
    assertEquals(0, response.getItems().size());

    logger.warn("testGetCart_WhenCartExists passed ✅");
  }

  @Test
  void testAddToCart() {
    logger.warn("Running testAddToCart 🚀");

    Long userId = 100L;
    Long productId = 200L;
    int quantity = 3;

    cartService.addToCart(userId, productId, quantity);

    verify(cartProducer, times(1)).sendMessage(any());

    logger.warn("testAddToCart passed ✅");
  }

  @Test
  void testUpdateCartItem() {
    logger.warn("Running testUpdateCartItem 🚀");

    Long userId = 100L;
    Long productId = 200L;
    int quantity = 5;

    cartService.updateCartItem(userId, productId, quantity);

    verify(cartProducer, times(1)).sendMessage(any());

    logger.warn("testUpdateCartItem passed ✅");
  }

  @Test
  void testRemoveFromCart() {
    logger.warn("Running testRemoveFromCart 🚀");

    Long userId = 100L;
    Long productId = 200L;

    cartService.removeFromCart(userId, productId);

    verify(cartProducer, times(1)).sendMessage(any());

    logger.warn("testRemoveFromCart passed ✅");
  }

  @Test
  void testClearCart() {
    logger.warn("Running testClearCart 🚀");

    Long userId = 100L;

    cartService.clearCart(userId);

    verify(cartProducer, times(1)).sendMessage(any());

    logger.warn("testClearCart passed ✅");
  }
}

