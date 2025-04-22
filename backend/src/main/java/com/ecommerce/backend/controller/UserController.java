package com.ecommerce.backend.controller;

import com.ecommerce.backend.model.User;
import com.ecommerce.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

  @Autowired
  private UserService userService;

  @PostMapping("/register")
  public String registerUser(@RequestBody User user) {
    return userService.registerUser(user);
  }
}
