package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.AddressResponse;
import com.ecommerce.backend.model.User;
import com.ecommerce.backend.service.AddressService;
import com.ecommerce.backend.service.UserService;
import jakarta.validation.Valid;
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
@RequestMapping("/address")
public class AddressController {

  private final AddressService addressService;
  private final UserService userService;

  public AddressController(AddressService addressService, UserService userService) {
    this.addressService = addressService;
    this.userService = userService;
  }

  @GetMapping
  public ResponseEntity<?> getAddress() {
    Long userId = getAuthenticatedUserId();
    try {
      AddressResponse address = addressService.getAddress(userId);
      return ResponseEntity.ok(address);
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
  }

  @PostMapping
  public ResponseEntity<?> newAddress(@RequestBody @Valid AddressResponse addressResponse) {
    Long userId = getAuthenticatedUserId();
    try {
      addressService.addAddress(userId, addressResponse.getAddressLine1(),
          addressResponse.getAddressLine2(), addressResponse.getCity(),
          addressResponse.getDoorNumber(), addressResponse.getPinCode());
      return ResponseEntity.ok("Address added successfully.");
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }
  }

  @PutMapping
  public ResponseEntity<?> updateAddress(@RequestBody @Valid AddressResponse addressResponse) {
    Long userId = getAuthenticatedUserId();
    try {
      addressService.updateAddress(userId, addressResponse);
      return ResponseEntity.ok("Address updated successfully.");
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
  }

  @DeleteMapping
  public ResponseEntity<?> deleteAddress() {
    Long userId = getAuthenticatedUserId();
    try {
      addressService.deleteAddress(userId);
      return ResponseEntity.ok("Address deleted successfully.");
    } catch (RuntimeException e) {
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
