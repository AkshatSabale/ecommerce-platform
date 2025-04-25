package com.ecommerce.backend.controller;

import com.ecommerce.backend.model.Cart;
import com.ecommerce.backend.model.User;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.service.CartService;
import com.ecommerce.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController {

  private final ProductRepository repository;
  private final UserService userService;
  private final CartService cartService;

  public CartController(ProductRepository repository, UserService userService, CartService cartService) {
    this.repository = repository;
    this.userService = userService;
    this.cartService = cartService;
  }

  @GetMapping("/getCartByUser")
  public ResponseEntity<Cart> userCart() {
    User user = userService.getLoggedInUser(); // Assuming this fetches the current user
    Cart cart = cartService.getCartByUser(user);
    return ResponseEntity.ok(cart);
  }
}
