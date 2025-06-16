package com.ecommerce.backend.repository;


import com.ecommerce.backend.model.Cart;
import com.ecommerce.backend.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

  Optional<Cart> findByUserId(Long userId);

  @Modifying
  @Query("DELETE FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.product.id = :productId")
  void deleteCartItem(Long cartId, Long productId);

  @Modifying
  @Transactional
  @Query("DELETE FROM CartItem ci WHERE ci.product.id = :productId")
  void deleteCartItemsByProductId(@Param("productId") Long productId);
}
