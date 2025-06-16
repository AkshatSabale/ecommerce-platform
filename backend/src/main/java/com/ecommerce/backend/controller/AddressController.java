package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.AddressResponse;
import com.ecommerce.backend.model.User;
import com.ecommerce.backend.service.AddressService;
import com.ecommerce.backend.service.UserService;
import com.ecommerce.backend.util.AuthUtil;
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

  private final AuthUtil authUtil;

  public AddressController(AddressService addressService, UserService userService,
      AuthUtil authUtil) {
    this.addressService = addressService;
    this.userService = userService;
    this.authUtil = authUtil;
  }

  @GetMapping
  public ResponseEntity<?> getAddress() {
    try {
      Long userId = authUtil.getAuthenticatedUserId();
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
      Long userId = authUtil.getAuthenticatedUserId();
      addressService.addAddress(userId, addressResponse);
      return ResponseEntity.ok("Address added successfully.");
    } catch (IllegalStateException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }
  }

  @PutMapping
  public ResponseEntity<?> updateAddress(@RequestBody @Valid AddressResponse addressResponse) {
    try {
      Long userId = authUtil.getAuthenticatedUserId();
      addressService.updateAddress(userId, addressResponse);
      return ResponseEntity.ok("Address updated successfully.");
    } catch (NoSuchElementException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
  }

  @DeleteMapping
  public ResponseEntity<?> deleteAddress() {
    try {
      Long userId = authUtil.getAuthenticatedUserId();
      addressService.deleteAddress(userId);
      return ResponseEntity.ok("Address deleted successfully.");
    } catch (NoSuchElementException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
  }


}
