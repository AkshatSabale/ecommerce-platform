package com.ecommerce.backend.service;

import com.ecommerce.backend.model.Cart;
import com.ecommerce.backend.model.User;
import com.ecommerce.backend.repository.CartRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CartService {

  private final CartRepository cartRepository;

  public CartService(CartRepository cartRepository) {
    this.cartRepository = cartRepository;
  }

  public Cart getCartByUser(User user) {
    return cartRepository.findByUser(user)
        .orElseGet(() -> {
          Cart newCart = new Cart();
          newCart.setUser(user);
          newCart.setUpdatedAt(LocalDateTime.now());
          return cartRepository.save(newCart);
        });
  }
}