package com.ecommerce.backend.service;


import com.ecommerce.backend.model.User;
import com.ecommerce.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {
  private final UserRepository userRepository;
  public UserService(UserRepository userRepository, EmailService emailService) {
    this.userRepository = userRepository;
  }

  public List<User> allUsers() {
    List<User> users = new ArrayList<>();
    userRepository.findAll().forEach(users::add);
    return users;
  }
}