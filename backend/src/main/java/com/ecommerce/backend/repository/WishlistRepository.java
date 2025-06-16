package com.ecommerce.backend.repository;

import com.ecommerce.backend.model.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist,Long> {
  Optional<Wishlist> findByUserId(Long userId);
}