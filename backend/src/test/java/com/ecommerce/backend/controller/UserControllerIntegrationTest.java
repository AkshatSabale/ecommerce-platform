package com.ecommerce.backend.controller;

import com.ecommerce.backend.model.User;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
public class UserControllerIntegrationTest extends BaseIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  @WithMockUser(username = "testuser", roles = {"CUSTOMER", "ADMIN"})
  public void getCurrentUser_AuthenticatedUser_ReturnsUser() throws Exception {
    MvcResult result = mockMvc.perform(get("/users/me"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value(TEST_USERNAME))
        .andExpect(jsonPath("$.email").value("testuser@example.com"))
        .andReturn();
  }

  @Test
  public void getCurrentUser_UnauthenticatedUser_ReturnsUnauthorized() throws Exception {
    mockMvc.perform(get("/users/me"))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  public void allUsers_AdminAccess_ReturnsUsersList() throws Exception {
    mockMvc.perform(get("/users/"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].username").value(TEST_USERNAME));
  }

  @Test
  @WithMockUser(roles = "CUSTOMER")
  public void allUsers_NonAdminAccess_ReturnsForbidden() throws Exception {
    mockMvc.perform(get("/users/"))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(username = "testuser")
  public void getPurchasedProducts_ValidUser_ReturnsProductsList() throws Exception {
    // First add some products to the user
    User user = userRepository.findByUsername(TEST_USERNAME).orElseThrow();
    user.setProductsPurchased(List.of(1L, 2L, 3L));
    userRepository.save(user);

    mockMvc.perform(get("/users/users/{userId}/purchased-products", user.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0]").value(1))
        .andExpect(jsonPath("$[1]").value(2))
        .andExpect(jsonPath("$[2]").value(3));
  }

  @Test
  @WithMockUser
  public void getPurchasedProducts_InvalidUser_ReturnsNotFound() throws Exception {
    mockMvc.perform(get("/users/users/{userId}/purchased-products", 999))
        .andExpect(status().isNotFound());
  }
}