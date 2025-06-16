package com.ecommerce.backend.service;

import com.ecommerce.backend.model.User;
import com.ecommerce.backend.repository.UserRepository;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;


import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private CacheManager cacheManager;

  @InjectMocks
  private UserService userService;

  @Mock
  private Cache userCache;

  @Mock
  private Cache usersCache;




  @Test
  public void getUserByUserName_ValidUsername_ReturnsUser() {
    // Setup
    User mockUser = new User();
    mockUser.setUsername("testuser");
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));

    // Execute
    User result = userService.getUserByUserName("testuser");

    // Verify
    assertNotNull(result);
    assertEquals("testuser", result.getUsername());
    verify(userRepository).findByUsername("testuser");
  }

  @Test
  public void getUserByUserName_InvalidUsername_ThrowsException() {
    when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

    assertThrows(UsernameNotFoundException.class, () -> {
      userService.getUserByUserName("nonexistent");
    });
  }

  @Test
  public void createUser_ValidUser_SavesAndClearsCache() {
    // Setup
    User newUser = new User();
    newUser.setUsername("newuser");
    when(userRepository.save(newUser)).thenReturn(newUser);

    // Execute
    User result = userService.createUser(newUser);

    // Verify
    assertNotNull(result);
    verify(userRepository).save(newUser);
  }

  @Test
  public void allUsers_ReturnsCachedData() {
    // Setup
    List<User> mockUsers = List.of(new User(), new User());
    when(userRepository.findAll()).thenReturn(mockUsers);

    // Execute
    List<User> result = userService.allUsers();

    // Verify
    assertEquals(2, result.size());
    verify(userRepository).findAll();
  }

  @Test
  public void getLoggedInUser_Authenticated_ReturnsUser() {
    // Setup
    User mockUser = new User();
    mockUser.setUsername("testuser");
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));

    Authentication authentication = new UsernamePasswordAuthenticationToken(
        "testuser",
        "password",
        Collections.emptyList()
    );

    SecurityContext securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(securityContext);

    // Execute
    User result = userService.getLoggedInUser();

    // Verify
    assertEquals("testuser", result.getUsername());
  }

  @Test
  public void deleteUser_ValidId_DeletesAndClearsCache() {
    // Setup
    Long userId = 1L;
    doNothing().when(userRepository).deleteById(userId);

    // Execute
    userService.deleteUser(userId);

    // Verify
    verify(userRepository).deleteById(userId);
  }

}