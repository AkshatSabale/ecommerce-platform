package com.ecommerce.backend.service;


import com.ecommerce.backend.dto.WishlistResponse;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.model.User;
import com.ecommerce.backend.model.Wishlist;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.repository.UserRepository;
import com.ecommerce.backend.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WishlistService {

  private final WishlistRepository wishlistRepo;
  private final ProductRepository productRepo; // only to validate product exists

  /** Ensure wishlist exists */
  private Wishlist getOrCreate(Long userId) {
    return wishlistRepo.findByUserId(userId)
        .orElseGet(() -> {
          Wishlist w = new Wishlist();
          w.setUserId(userId);            // or w.setUser(userEntity)
          return wishlistRepo.save(w);
        });
  }

  /* ---------- Public API ---------- */

  public WishlistResponse getWishlist(Long userId) {
    Wishlist w = getOrCreate(userId);
    return new WishlistResponse(w.getId(), w.getProductIds());
  }

  @Transactional
  public WishlistResponse addProduct(Long userId, Long productId) {
    // validate product
    if (!productRepo.existsById(productId)) {
      throw new ResourceNotFoundException("Product", "id", productId);
    }
    Wishlist w = getOrCreate(userId);
    w.getProductIds().add(productId);
    wishlistRepo.save(w);
    return new WishlistResponse(w.getId(), w.getProductIds());
  }

  @Transactional
  public WishlistResponse removeProduct(Long userId, Long productId) {
    Wishlist w = getOrCreate(userId);
    w.getProductIds().remove(productId);
    wishlistRepo.save(w);
    return new WishlistResponse(w.getId(), w.getProductIds());
  }

  @Transactional
  public void clear(Long userId) {
    Wishlist w = getOrCreate(userId);
    w.getProductIds().clear();
    wishlistRepo.save(w);
  }
}