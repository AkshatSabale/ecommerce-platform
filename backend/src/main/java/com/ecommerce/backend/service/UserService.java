package com.ecommerce.backend.service;


import com.ecommerce.backend.model.User;
import com.ecommerce.backend.repository.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

  private final UserRepository userRepository;

  // Cache for all users list
  @Cacheable(value = "users", key = "'allUsers'")
  public List<User> allUsers() {
    log.info("Fetching all users from database");
    return (List<User>) userRepository.findAll();
  }

  // Cache individual user by username
  @Cacheable(value = "user", key = "'username:' + #username")
  public User getUserByUserName(String username) {
    log.info("Fetching user by username from database: {}", username);
    return userRepository.findByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
  }

  // Cache individual user by email
  @Cacheable(value = "user", key = "'email:' + #email")
  public User getUserByEmail(String email) {
    log.info("Fetching user by email from database: {}", email);
    return userRepository.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
  }

  // Cache individual user by ID
  @Cacheable(value = "user", key = "'id:' + #id")
  public User getUserById(Long id) {
    log.info("Fetching user by ID from database: {}", id);
    return userRepository.findById(id)
        .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
  }

  // Cache the currently logged in user
  @Cacheable(value = "user", key = "'current:' + #authentication.name")
  public User getLoggedInUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated() ||
        authentication.getPrincipal().equals("anonymousUser")) {
      throw new UsernameNotFoundException("No authenticated user found");
    }

    String username = authentication.getName();
    log.info("Fetching logged in user from database: {}", username);
    return getUserByUserName(username);
  }

  // Clear relevant caches when a user is created/updated
  @CacheEvict(value = {"users", "user"}, allEntries = true)
  public User createUser(User user) {
    log.info("Creating new user: {}", user.getUsername());
    return userRepository.save(user);
  }



  // Clear relevant caches when a user is deleted
  @CacheEvict(value = {"users", "user"}, allEntries = true)
  public void deleteUser(Long id) {
    log.info("Deleting user with ID: {}", id);
    userRepository.deleteById(id);
  }
}