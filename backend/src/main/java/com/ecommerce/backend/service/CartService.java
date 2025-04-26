package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.CartDto;
import com.ecommerce.backend.dto.CartItemDto;
import com.ecommerce.backend.model.Cart;
import com.ecommerce.backend.model.CartItem;
import com.ecommerce.backend.model.User;
import com.ecommerce.backend.repository.CartRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
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

  public CartDto mapToDto(Cart cart) {
    CartDto dto = new CartDto();
    dto.setId(cart.getId());
    dto.setUserEmail(cart.getUser().getEmail());
    dto.setUpdatedAt(cart.getUpdatedAt());

    List<CartItemDto> items = cart.getItems().stream().map(item -> {
      CartItemDto itemDto = new CartItemDto();
      itemDto.setId(item.getId());
      itemDto.setProductName("Soap"); // assuming Product entity exists
      return itemDto;
    }).collect(Collectors.toList());

    dto.setItems(items);
    return dto;
  }

  public Cart addToCart(User user, Long productId, Long quantity) {
    Cart cart = getCartByUser(user); // fetch or create

    // Check if item already exists
    Optional<CartItem> existingItem = cart.getItems().stream()
        .filter(item -> item.getProduct().getId().equals(productId))
        .findFirst();

    if (existingItem.isPresent()) {
      // Update quantity
      CartItem item = existingItem.get();
      item.setQuantity(item.getQuantity() + quantity);
    } else {
      // Add new item
      CartItem newItem = new CartItem();
      newItem.setCart(cart);
      newItem.setQuantity(quantity);

      cart.getItems().add(newItem);
    }

    cart.setUpdatedAt(LocalDateTime.now());
    return cartRepository.save(cart);
  }
}