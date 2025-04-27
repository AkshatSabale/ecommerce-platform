package com.ecommerce.backend.controller;


import com.ecommerce.backend.dto.LoginUserDto;
import com.ecommerce.backend.model.User;
import com.ecommerce.backend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RequestMapping("/users")
@RestController
public class UserController {
  private final UserService userService;
  public UserController(UserService userService) {
    this.userService = userService;
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
}
