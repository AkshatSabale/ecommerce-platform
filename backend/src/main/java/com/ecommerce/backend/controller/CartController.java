package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.AddToCartRequest;
import com.ecommerce.backend.dto.CartDto;
import com.ecommerce.backend.model.Cart;
import com.ecommerce.backend.model.User;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.service.CartService;
import com.ecommerce.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController {

  private final ProductRepository repository;
  private final UserService userService;
  private final CartService cartService;

  public CartController(ProductRepository repository, UserService userService,
      CartService cartService) {
    this.repository = repository;
    this.userService = userService;
    this.cartService = cartService;
  }

  @GetMapping("/getCartByUser")
  public ResponseEntity<CartDto> userCart() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String email = authentication.getName(); // extract the username/email from the token
    User currentUser = userService.getUserByEmail(email);
    Cart cart = cartService.getCartByUser(currentUser);
    return ResponseEntity.ok(cartService.mapToDto(cart));
  }

  @PostMapping("/insertIntoCart")
  public ResponseEntity<CartDto> addToCart(@RequestBody AddToCartRequest request) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String email = authentication.getName();
    User currentUser = userService.getUserByEmail(email);

    Cart updatedCart = cartService.addToCart(currentUser, request.getProductId(),
        request.getQuantity());

    return ResponseEntity.ok(cartService.mapToDto(updatedCart));
  }
}
