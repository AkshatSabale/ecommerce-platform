package com.ecommerce.backend.controller;

import com.ecommerce.backend.model.Role;
import com.ecommerce.backend.model.User;
import com.ecommerce.backend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;


import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
public class AuthenticationControllerIntegrationTest extends BaseIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @MockBean
  private AuthenticationManager authenticationManager;

  @Test
  @WithMockUser(roles = "ADMIN")
  public void checkAdmin_WhenUserIsAdmin_ReturnsTrue() throws Exception {
    mockMvc.perform(get("/api/auth/check-admin"))
        .andExpect(status().isOk())
        .andExpect(content().string("true"));
  }

  @Test
  @WithMockUser(roles = "CUSTOMER")
  public void checkAdmin_WhenUserIsNotAdmin_ReturnsFalse() throws Exception {
    mockMvc.perform(get("/api/auth/check-admin"))
        .andExpect(status().isOk())
        .andExpect(content().string("false"));
  }

  @Test
  public void signup_ValidInput_CreatesUserAndReturnsIt() throws Exception {
    Map<String, String> registerData = new HashMap<>();
    registerData.put("username", "newuser");
    registerData.put("email", "newuser@example.com");
    registerData.put("password", "password123");

    MvcResult result = mockMvc.perform(post("/api/auth/signup")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registerData)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value("newuser"))
        .andExpect(jsonPath("$.email").value("newuser@example.com"))
        .andReturn();

    // Verify user is saved in DB with verification code
    User savedUser = userRepository.findByEmail("newuser@example.com").orElse(null);
    assertNotNull(savedUser);
    assertNotNull(savedUser.getVerificationCode());
    assertNotNull(savedUser.getVerificationCodeExpiresAt());
    assertFalse(savedUser.isEnabled());
  }



  @Test
  public void login_WithUnverifiedAccount_ReturnsError() throws Exception {
    // Create an unverified user
    User user = new User();
    user.setUsername("unverified");
    user.setEmail("unverified@example.com");
    user.setPassword(passwordEncoder.encode("password"));
    user.setEnabled(false);
    userRepository.save(user);

    // Mock the behavior to throw the exception
    when(authenticationManager.authenticate(any()))
        .thenThrow(new BadCredentialsException("Account not verified"));

    Map<String, String> loginData = new HashMap<>();
    loginData.put("email", "unverified@example.com");
    loginData.put("password", "password");

    mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginData)))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Account not verified. Please verify your account."));
  }

  @Test
  public void login_WithValidCredentials_ReturnsToken() throws Exception {
    // First create a verified user with roles
    User user = new User();
    user.setUsername("loginuser");
    user.setEmail("loginuser@example.com");
    user.setPassword(passwordEncoder.encode("password"));
    user.setEnabled(true);

    Role customerRole = roleRepository.findByName("ROLE_CUSTOMER")
        .orElseThrow(() -> new RuntimeException("Role not found"));
    user.getRoles().add(customerRole);

    userRepository.save(user);

    // Mock authentication
    when(authenticationManager.authenticate(any()))
        .thenReturn(new UsernamePasswordAuthenticationToken(
            user.getEmail(),
            user.getPassword(),
            user.getAuthorities()));

    Map<String, String> loginData = new HashMap<>();
    loginData.put("email", "loginuser@example.com");
    loginData.put("password", "password");

    mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginData)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").exists())
        .andExpect(jsonPath("$.expiresIn").exists());
  }

  @Test
  public void verifyUser_WithValidCode_VerifiesUser() throws Exception {
    // Create an unverified user with verification code
    User user = new User();
    user.setUsername("verifyuser");
    user.setEmail("verifyuser@example.com");
    user.setPassword("password");
    user.setVerificationCode("123456");
    user.setVerificationCodeExpiresAt(LocalDateTime.now().plusHours(1));
    user.setEnabled(false);
    userRepository.save(user);

    Map<String, String> verifyData = new HashMap<>();
    verifyData.put("email", "verifyuser@example.com");
    verifyData.put("verificationCode", "123456");

    mockMvc.perform(post("/api/auth/verify")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(verifyData)))
        .andExpect(status().isOk())
        .andExpect(content().string("Account verified successfully"));

    // Verify user is now enabled
    User verifiedUser = userRepository.findByEmail("verifyuser@example.com").orElse(null);
    assertTrue(verifiedUser.isEnabled());
  }

  @Test
  public void verifyUser_WithExpiredCode_ReturnsError() throws Exception {
    // Create an unverified user with expired verification code
    User user = new User();
    user.setUsername("expireduser");
    user.setEmail("expireduser@example.com");
    user.setPassword("password");
    user.setVerificationCode("123456");
    user.setVerificationCodeExpiresAt(LocalDateTime.now().minusHours(1));
    user.setEnabled(false);
    userRepository.save(user);

    Map<String, String> verifyData = new HashMap<>();
    verifyData.put("email", "expireduser@example.com");
    verifyData.put("verificationCode", "123456");

    mockMvc.perform(post("/api/auth/verify")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(verifyData)))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Verification code has expired"));
  }

  @Test
  public void resendVerificationCode_ForUnverifiedUser_SendsNewCode() throws Exception {
    // Create an unverified user
    User user = new User();
    user.setUsername("resenduser");
    user.setEmail("resenduser@example.com");
    user.setPassword("password");
    user.setVerificationCode("123456");
    user.setVerificationCodeExpiresAt(LocalDateTime.now().minusHours(1));
    user.setEnabled(false);
    userRepository.save(user);

    mockMvc.perform(post("/api/auth/resend")
            .param("email", "resenduser@example.com"))
        .andExpect(status().isOk())
        .andExpect(content().string("Verification code sent"));

    // Verify new code was generated
    User updatedUser = userRepository.findByEmail("resenduser@example.com").orElse(null);
    assertNotNull(updatedUser);
    assertNotEquals("123456", updatedUser.getVerificationCode());
    assertTrue(updatedUser.getVerificationCodeExpiresAt().isAfter(LocalDateTime.now()));
  }

  @Test
  public void logout_ReturnsSuccessMessage() throws Exception {
    mockMvc.perform(post("/api/auth/logout"))
        .andExpect(status().isOk())
        .andExpect(content().string("Logged out successfully."));
  }
}