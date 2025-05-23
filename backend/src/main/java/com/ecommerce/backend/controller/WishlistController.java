package com.ecommerce.backend.controller;


import com.ecommerce.backend.dto.AddToWishlistRequest;
import com.ecommerce.backend.dto.WishlistResponse;
import com.ecommerce.backend.model.User;
import com.ecommerce.backend.service.UserService;
import com.ecommerce.backend.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wishlist")
@RequiredArgsConstructor
public class WishlistController {

  private final WishlistService wishlistService;
  private final UserService userService;

  private Long getAuthenticatedUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new SecurityException("User not authenticated");
    }

    Object principal = authentication.getPrincipal();
    if (!(principal instanceof UserDetails)) {
      throw new SecurityException("Invalid authentication principal");
    }

    String username = ((UserDetails) principal).getUsername();
    User user = userService.getUserByUserName(username);

    return user.getId();
  }

  /* ---------- endpoints ---------- */

  @GetMapping
  public ResponseEntity<WishlistResponse> getWishlist() {
    Long userId=getAuthenticatedUserId();
    return ResponseEntity.ok(wishlistService.getWishlist(userId));
  }

  @PostMapping("/items")
  public ResponseEntity<WishlistResponse> addToWishlist(@RequestBody AddToWishlistRequest req) {
    Long userId=getAuthenticatedUserId();
    return ResponseEntity.ok(
        wishlistService.addProduct(userId, req.getProductId()));
  }

  @DeleteMapping("/items/{productId}")
  public ResponseEntity<WishlistResponse> removeFromWishlist(@PathVariable Long productId) {
    Long userId=getAuthenticatedUserId();
    return ResponseEntity.ok(
        wishlistService.removeProduct(userId, productId));
  }

  @DeleteMapping
  public ResponseEntity<Void> clearWishlist() {
    Long userId=getAuthenticatedUserId();
    wishlistService.clear(userId);
    return ResponseEntity.noContent().build();
  }
}

