package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.CartItemDto;
import com.ecommerce.backend.dto.CartResponse;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.kafka.CartMessage;
import com.ecommerce.backend.kafka.CartProducer;
import com.ecommerce.backend.model.Cart;
import com.ecommerce.backend.model.CartItem;
import com.ecommerce.backend.model.Product;
import com.ecommerce.backend.model.User;
import com.ecommerce.backend.repository.CartRepository;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartService {

  private final CartRepository cartRepository;
  private final ProductRepository productRepository;
  private final UserRepository userRepository;
  private final CartProducer cartProducer;

  @Cacheable(value = "cart", key = "#userId")
  public CartResponse getCart(Long userId) {
    Cart cart = cartRepository.findByUserId(userId)
        .orElseGet(() -> createNewCart(userId));
    return mapToCartResponse(cart);
  }

  @CacheEvict(value = "cart", key = "#userId")
  public void addToCart(Long userId, Long productId, int quantity) {
    cartProducer.sendMessage(new CartMessage(userId, "ADD", productId, quantity));
  }

  @CacheEvict(value = "cart", key = "#userId")
  public void updateCartItem(Long userId, Long productId, int quantity) {
    cartProducer.sendMessage(new CartMessage(userId, "UPDATE", productId, quantity));
  }

  @CacheEvict(value = "cart", key = "#userId")
  public void removeFromCart(Long userId, Long productId) {
    cartProducer.sendMessage(new CartMessage(userId, "REMOVE", productId, 0));
  }

  @CacheEvict(value = "cart", key = "#userId")
  public void clearCart(Long userId) {
    cartProducer.sendMessage(new CartMessage(userId, "CLEAR", null, 0));
  }

  @CacheEvict(value = "cart", key = "#userId")
  private Cart createNewCart(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    Cart newCart = new Cart();
    newCart.setUser(user);
    return cartRepository.save(newCart);
  }

  private CartResponse mapToCartResponse(Cart cart) {
    List<CartItemDto> itemDtos = cart.getItems().stream().map(item -> new CartItemDto(
        item.getProduct().getId(),
        item.getProduct().getName(),
        item.getProduct().getPrice(),
        item.getQuantity()
    )).collect(Collectors.toList());

    return new CartResponse(
        cart.getId(),
        itemDtos,
        cart.getUpdatedAt()
    );
  }
}