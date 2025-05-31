package com.ecommerce.backend.service;


import com.ecommerce.backend.dto.WishlistResponse;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.kafka.WishlistMessage;
import com.ecommerce.backend.kafka.WishlistProducer;
import com.ecommerce.backend.model.User;
import com.ecommerce.backend.model.Wishlist;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.repository.UserRepository;
import com.ecommerce.backend.repository.WishlistRepository;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WishlistService {

  private final WishlistRepository wishlistRepo;
  private final ProductRepository productRepo;
  private final WishlistProducer wishlistProducer;

  @Cacheable(value = "wishlists", key = "#userId")
  public WishlistResponse getWishlist(Long userId) {
    Wishlist w = wishlistRepo.findByUserId(userId)
        .orElseGet(() -> {
          Wishlist newW = new Wishlist();
          newW.setUserId(userId);
          return wishlistRepo.save(newW);
        });
    Set<Long> productIds = new HashSet<>(w.getProductIds());
    return new WishlistResponse(w.getId(), productIds);
  }

  public WishlistResponse addProduct(Long userId, Long productId) {
    WishlistMessage msg = new WishlistMessage("ADD", userId, productId);
    wishlistProducer.sendMessage(msg);
    // Return current cached wishlist or empty placeholder while async update occurs
    return getWishlist(userId);
  }

  public WishlistResponse removeProduct(Long userId, Long productId) {
    WishlistMessage msg = new WishlistMessage("REMOVE", userId, productId);
    wishlistProducer.sendMessage(msg);
    return getWishlist(userId);
  }

  public void clear(Long userId) {
    WishlistMessage msg = new WishlistMessage("CLEAR", userId, null);
    wishlistProducer.sendMessage(msg);
  }
}