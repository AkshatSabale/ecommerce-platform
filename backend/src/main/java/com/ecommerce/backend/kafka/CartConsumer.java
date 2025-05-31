package com.ecommerce.backend.kafka;

import com.ecommerce.backend.model.Cart;
import com.ecommerce.backend.model.CartItem;
import com.ecommerce.backend.model.Product;
import com.ecommerce.backend.model.User;
import com.ecommerce.backend.kafka.CartMessage;
import com.ecommerce.backend.repository.CartRepository;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CartConsumer {

  private final CartRepository cartRepository;
  private final UserRepository userRepository;
  private final ProductRepository productRepository;

  @KafkaListener(topics = "cart-topic", groupId = "cart_group")
  public void consume(String messageJson) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      CartMessage message = mapper.readValue(messageJson, CartMessage.class);

      Long userId = message.getUserId();
      Long productId = message.getProductId();
      int quantity = message.getQuantity();

      Cart cart = cartRepository.findByUserId(userId)
          .orElseGet(() -> createNewCart(userId));
      cart.getItems().size();

      switch (message.getOperation()) {
        case "ADD":
          Product productToAdd = productRepository.findById(productId)
              .orElseThrow(() -> new RuntimeException("Product not found"));

          Optional<CartItem> existingItem = cart.getItems().stream()
              .filter(i -> i.getProduct().getId().equals(productId))
              .findFirst();

          if (existingItem.isPresent()) {
            existingItem.get().setQuantity(existingItem.get().getQuantity() + quantity);
          } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(productToAdd);
            newItem.setQuantity(quantity);
            cart.getItems().add(newItem);
          }

          cart.setUpdatedAt(LocalDateTime.now());
          cartRepository.save(cart);
          break;

        case "UPDATE":
          CartItem itemToUpdate = cart.getItems().stream()
              .filter(i -> i.getProduct().getId().equals(productId))
              .findFirst()
              .orElseThrow(() -> new RuntimeException("Item not found in cart"));

          itemToUpdate.setQuantity(quantity);
          cart.setUpdatedAt(LocalDateTime.now());
          cartRepository.save(cart);
          break;

        case "REMOVE":
          cart.getItems().removeIf(item -> item.getProduct().getId().equals(productId));
          cart.setUpdatedAt(LocalDateTime.now());
          cartRepository.save(cart);
          break;

        case "CLEAR":
          cart.getItems().clear();
          cart.setUpdatedAt(LocalDateTime.now());
          cartRepository.save(cart);
          break;

        default:
          System.out.println("Unknown operation: " + message.getOperation());
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private Cart createNewCart(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));

    Cart newCart = new Cart();
    newCart.setUser(user);
    newCart.setCreatedAt(LocalDateTime.now());
    newCart.setUpdatedAt(LocalDateTime.now());
    return cartRepository.save(newCart);
  }
}
