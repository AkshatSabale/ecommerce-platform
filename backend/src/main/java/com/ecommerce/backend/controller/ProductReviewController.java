package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.ProductReviewRequest;
import com.ecommerce.backend.dto.ProductReviewResponse;
import com.ecommerce.backend.model.User;
import com.ecommerce.backend.service.ProductReviewService;
import com.ecommerce.backend.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@Slf4j
public class ProductReviewController {

  private final ProductReviewService reviewService;
  private final UserService userService;

  // 🔐 Authenticated - Add or update review
  @PostMapping
  public ResponseEntity<ProductReviewResponse> addOrUpdateReview(@RequestBody @Valid ProductReviewRequest request) {
    Long userId = getAuthenticatedUserId();
    return ResponseEntity.ok(reviewService.addOrUpdateReview(userId, request));
  }

  // 🔐 Authenticated - Delete review
  @DeleteMapping("/{productId}")
  public ResponseEntity<Void> deleteReview(@PathVariable Long productId) {
    Long userId = getAuthenticatedUserId();
    reviewService.deleteReview(userId, productId);
    return ResponseEntity.noContent().build();
  }

  // 🔓 Public - Get all reviews for a product
  @GetMapping("/product/{productId}")
  public ResponseEntity<List<ProductReviewResponse>> getReviewsForProduct(@PathVariable Long productId) {
    return ResponseEntity.ok(reviewService.getReviewsForProduct(productId));
  }

  // 🔓 Public - Get average rating
  @GetMapping("/product/{productId}/average")
  public ResponseEntity<Double> getAverageRating(@PathVariable Long productId) {
    Double average = reviewService.getAverageRatingForProduct(productId);
    return ResponseEntity.ok(average != null ? average : 0.0);
  }

  // Utility: used to fetch currently authenticated user ID
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
}
