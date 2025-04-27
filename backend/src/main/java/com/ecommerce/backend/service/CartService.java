package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.CartItemDto;
import com.ecommerce.backend.dto.CartResponse;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.model.Cart;
import com.ecommerce.backend.model.CartItem;
import com.ecommerce.backend.model.Product;
import com.ecommerce.backend.model.User;
import com.ecommerce.backend.repository.CartRepository;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartService {

  private final CartRepository cartRepository;
  private final ProductRepository productRepository;
  private final UserRepository userRepository;

  public CartResponse getCart(Long userId) {
    Cart cart = cartRepository.findByUserId(userId)
        .orElseGet(() -> createNewCart(userId));
    return mapToCartResponse(cart);
  }

  public CartResponse addToCart(Long userId, Long productId, int quantity) {
    Cart cart = getCartEntity(userId);
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

    Optional<CartItem> existingItem = cart.getItems().stream()
        .filter(item -> item.getProduct().getId().equals(productId))
        .findFirst();

    if (existingItem.isPresent()) {
      existingItem.get().setQuantity(existingItem.get().getQuantity() + quantity);
    } else {
      CartItem newItem = new CartItem();
      newItem.setCart(cart);
      newItem.setProduct(product);
      newItem.setQuantity(quantity);
      cart.getItems().add(newItem);
    }

    cart = cartRepository.save(cart);
    return mapToCartResponse(cart);
  }

  public CartResponse updateCartItem(Long userId, Long productId, int quantity) {
    if (quantity <= 0) {
      throw new IllegalArgumentException("Quantity must be greater than 0");
    }

    Cart cart = getCartEntity(userId);
    CartItem item = cart.getItems().stream()
        .filter(i -> i.getProduct().getId().equals(productId))
        .findFirst()
        .orElseThrow(() -> new ResourceNotFoundException("Item not found in cart"));

    item.setQuantity(quantity);
    cart = cartRepository.save(cart);
    return mapToCartResponse(cart);
  }

  public void removeFromCart(Long userId, Long productId) {
    Cart cart = getCartEntity(userId);
    cart.getItems().removeIf(item -> item.getProduct().getId().equals(productId));
    cartRepository.save(cart);
  }

  public void clearCart(Long userId) {
    Cart cart = getCartEntity(userId);
    cart.getItems().clear();
    cartRepository.save(cart);
  }

  private Cart createNewCart(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    Cart newCart = new Cart();
    newCart.setUser(user);
    return cartRepository.save(newCart);
  }

  private Cart getCartEntity(Long userId) {
    return cartRepository.findByUserId(userId)
        .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
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
