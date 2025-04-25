package com.ecommerce.backend.repository;


import com.ecommerce.backend.model.Cart;
import com.ecommerce.backend.model.User;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class CartRepository {
  private final Map<Long, Cart> cartStorage = new HashMap<>();

  public Optional<Cart> findByUser(User user) {
    return Optional.ofNullable(cartStorage.get(user.getId()));
  }

  public Cart save(Cart cart) {
    cartStorage.put(cart.getUser().getId(), cart);
    return cart;
  }
}
