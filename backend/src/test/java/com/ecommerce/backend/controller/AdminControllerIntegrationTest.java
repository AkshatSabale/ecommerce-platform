package com.ecommerce.backend.controller;


import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@AutoConfigureMockMvc
class AdminControllerIntegrationTest extends BaseIntegrationTest {

  private static final Logger logger = LoggerFactory.getLogger(AdminControllerIntegrationTest.class);

  @Autowired
  private MockMvc mockMvc;

  @Test
  @WithMockUser(username = TEST_USERNAME, roles = {"ADMIN"})
  void testAdminDashboardAccessAsAdmin() throws Exception {
    logger.warn("Running testAdminDashboardAccessAsAdmin 🚀");

    mockMvc.perform(get("/api/admin/dashboard")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().string("Welcome to Admin Dashboard"));

    logger.warn("testAdminDashboardAccessAsAdmin passed ✅");
  }

  @Test
  @WithMockUser(username = TEST_USERNAME, roles = {"CUSTOMER"})
  void testAdminDashboardAccessAsCustomer() throws Exception {
    logger.warn("Running testAdminDashboardAccessAsCustomer 🚀");

    mockMvc.perform(get("/api/admin/dashboard")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden()); // Access should be denied

    logger.warn("testAdminDashboardAccessAsCustomer passed ✅");
  }

  @Test
  void testAdminDashboardAccessAsAnonymous() throws Exception {
    logger.warn("Running testAdminDashboardAccessAsAnonymous 🚀");

    mockMvc.perform(get("/api/admin/dashboard")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden()); // No login = 401

    logger.warn("testAdminDashboardAccessAsAnonymous passed ✅");
  }
}
