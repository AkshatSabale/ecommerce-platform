package com.ecommerce.backend.controller;


import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.model.User;
import com.ecommerce.backend.repository.UserRepository;
import com.ecommerce.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RequestMapping("/users")
@RestController
public class UserController {
  private final UserService userService;
  private final UserRepository userRepository;
  public UserController(UserService userService, UserRepository userRepository) {
    this.userService = userService;
    this.userRepository = userRepository;
  }

  @GetMapping("/me")
  public ResponseEntity<User> getCurrentUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    // principal is UserDetails because your JWT filter set it that way
    UserDetails details = (UserDetails) auth.getPrincipal();
    User user = userService.getUserByUserName(details.getUsername());
    return ResponseEntity.ok(user);
  }

  @GetMapping("/")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<List<User>> allUsers() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    // principal is UserDetails because your JWT filter set it that way
    UserDetails details = (UserDetails) auth.getPrincipal();
    List <User> users = userService.allUsers();
    return ResponseEntity.ok(users);
  }

  @GetMapping("/users/{userId}/purchased-products")
  public ResponseEntity<List<Long>> getPurchasedProducts(@PathVariable Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    return ResponseEntity.ok(user.getProductsPurchased());
  }
}
