package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.AddressResponse;
import com.ecommerce.backend.model.User;
import com.ecommerce.backend.service.AddressService;
import com.ecommerce.backend.service.UserService;
import jakarta.validation.Valid;
import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/address")
public class AddressController {

  private final AddressService addressService;
  private final UserService userService;

  public AddressController(AddressService addressService, UserService userService) {
    this.addressService = addressService;
    this.userService = userService;
  }

  @GetMapping
  public ResponseEntity<?> getAddress() {
    try {
      Long userId = getAuthenticatedUserId();
      return ResponseEntity.ok(addressService.getAddress(userId));
    } catch (NoSuchElementException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    } catch (SecurityException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Please log in.");
    }
  }

  @PostMapping
  public ResponseEntity<?> addAddress(@RequestBody @Valid AddressResponse addressResponse) {
    try {
      Long userId = getAuthenticatedUserId();
      addressService.addAddress(userId, addressResponse);
      return ResponseEntity.ok("Address added successfully.");
    } catch (IllegalStateException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }
  }

  @PutMapping
  public ResponseEntity<?> updateAddress(@RequestBody @Valid AddressResponse addressResponse) {
    try {
      Long userId = getAuthenticatedUserId();
      addressService.updateAddress(userId, addressResponse);
      return ResponseEntity.ok("Address updated successfully.");
    } catch (NoSuchElementException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
  }

  @DeleteMapping
  public ResponseEntity<?> deleteAddress() {
    try {
      Long userId = getAuthenticatedUserId();
      addressService.deleteAddress(userId);
      return ResponseEntity.ok("Address deleted successfully.");
    } catch (NoSuchElementException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
  }

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
