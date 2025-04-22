package com.ecommerce.backend.service;

import com.ecommerce.backend.model.User;
import com.ecommerce.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  private final UserRepository userRepository;

  @Autowired
  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public String registerUser(User user) {
    if (userRepository.findByEmail(user.getEmail()).isPresent()) {
      return "User with this email already exists!";
    }

    // Hash the password before saving
    user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
    userRepository.save(user);
    return "User registered successfully!";
  }
}