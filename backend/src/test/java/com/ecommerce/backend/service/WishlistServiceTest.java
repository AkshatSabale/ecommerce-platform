package com.ecommerce.backend.service;


import com.ecommerce.backend.dto.WishlistResponse;
import com.ecommerce.backend.kafka.WishlistProducer;
import com.ecommerce.backend.model.Wishlist;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.repository.WishlistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import org.springframework.test.annotation.Rollback;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
@Rollback
class WishlistServiceTest {

  @InjectMocks
  private WishlistService wishlistService;

  @Mock
  private WishlistRepository wishlistRepository;

  @Mock
  private ProductRepository productRepository;

  @Mock
  private WishlistProducer wishlistProducer;

  private static final Logger logger = LoggerFactory.getLogger(WishlistServiceTest.class);

  @BeforeEach
  void setUp() {
    logger.warn("Running WishlistServiceTest setUp 🚀");
    Wishlist dummyWishlist = new Wishlist();
    dummyWishlist.setId(1L);
    dummyWishlist.setUserId(100L);
    dummyWishlist.setProductIds(new HashSet<>());

    when(wishlistRepository.findByUserId(100L)).thenReturn(Optional.empty());
    when(wishlistRepository.save(any())).thenReturn(dummyWishlist);
  }

  @Test
  void testGetWishlist_WhenExists() {
    logger.warn("Running testGetWishlist_WhenExists 🚀");

    Wishlist wishlist = new Wishlist();
    wishlist.setId(1L);
    wishlist.setUserId(100L);
    wishlist.setProductIds(new HashSet<>(Arrays.asList(1L, 2L)));

    when(wishlistRepository.findByUserId(100L)).thenReturn(Optional.of(wishlist));

    WishlistResponse response = wishlistService.getWishlist(100L);

    assertEquals(1L, response.getId());
    assertEquals(2, response.getProductIds().size());
    assertTrue(response.getProductIds().contains(1L));
    assertTrue(response.getProductIds().contains(2L));

    logger.warn("testGetWishlist_WhenExists passed ✅");
  }

  @Test
  void testGetWishlist_WhenNotExists() {
    logger.warn("Running testGetWishlist_WhenNotExists 🚀");

    when(wishlistRepository.findByUserId(100L)).thenReturn(Optional.empty());

    when(wishlistRepository.save(any())).thenAnswer(invocation -> {
      Wishlist w = invocation.getArgument(0);
      w.setId(1L); // simulate auto-generation
      return w;
    });

    WishlistResponse response = wishlistService.getWishlist(100L);

    assertEquals(1L, response.getId());
    assertEquals(0, response.getProductIds().size());

    logger.warn("testGetWishlist_WhenNotExists passed ✅");
  }

  @Test
  void testAddProduct() {
    logger.warn("Running testAddProduct 🚀");

    Long userId = 100L;
    Long productId = 10L;

    Wishlist wishlist = new Wishlist();
    wishlist.setId(1L);
    wishlist.setUserId(userId);
    wishlist.setProductIds(new HashSet<>());


    WishlistResponse response = wishlistService.addProduct(userId, productId);



    verify(wishlistProducer, times(1)).sendMessage(any());
    verify(wishlistRepository, times(1)).findByUserId(100L);

    logger.warn("testAddProduct passed ✅");
  }

  @Test
  void testRemoveProduct() {
    logger.warn("Running testRemoveProduct 🚀");

    Long userId = 100L;
    Long productId = 10L;

    Set<Long> productIds = new HashSet<>(Set.of(productId));
    Wishlist wishlist = new Wishlist();
    wishlist.setId(1L);
    wishlist.setUserId(userId);
    wishlist.setProductIds(productIds);

    wishlistService.removeProduct(userId,productId);

    verify(wishlistProducer, times(1)).sendMessage(any());
  //  verify(wishlistRepository, times(1)).findByUserId(100L);


    logger.warn("testRemoveProduct passed ✅");
  }

  @Test
  void testClear() {
    logger.warn("Running testClear 🚀");

    Long userId = 100L;

    Wishlist wishlist = new Wishlist();
    wishlist.setId(1L);
    wishlist.setUserId(userId);
    wishlist.setProductIds(new HashSet<>(Set.of(1L, 2L, 3L)));

    wishlistService.clear(userId);
  //  wishlistService.clear(userId);


    verify(wishlistProducer, times(1)).sendMessage(any());

    logger.warn("testClear passed ✅");
  }
}
