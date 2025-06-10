package com.ecommerce.backend.controller;


import com.ecommerce.backend.dto.AddressResponse;
import com.ecommerce.backend.model.Address;
import com.ecommerce.backend.model.User;
import com.ecommerce.backend.repository.AddressRepository;
import com.ecommerce.backend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AddressControllerIntegrationTest extends BaseIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private AddressRepository addressRepository;

  @MockBean
  private CacheManager cacheManager;

  private static final Logger logger = LoggerFactory.getLogger(AddressControllerIntegrationTest.class);

  @BeforeEach
  void setUpAddress() {
    logger.warn("Running AddressControllerIntegrationTest setUp 🚀");

    Cache mockCache = new ConcurrentMapCache("address");
    Cache userCache = new ConcurrentMapCache("user");

    when(cacheManager.getCache("address")).thenReturn(mockCache);
    when(cacheManager.getCache("user")).thenReturn(userCache);

    addressRepository.deleteAllInBatch();

    Address address = new Address();
    address.setUserId(userRepository.findByUsername(TEST_USERNAME).get().getId());
    address.setDoorNumber("12A");
    address.setAddressLine1("Line 1");
    address.setAddressLine2("Line 2");
    address.setPinCode(123456L);
    address.setCity("Test City");

    addressRepository.save(address);

    logger.warn("AddressControllerIntegrationTest setUp complete ✅");
  }

  @AfterEach
  void cleanupAddress() {
    logger.warn("Cleaning up AddressControllerIntegrationTest ✅");
    addressRepository.deleteAllInBatch();
  }

  @Test
  @WithMockUser(username = TEST_USERNAME)
  void testGetAddress() throws Exception {
    logger.warn("Running testGetAddress 🚀");

    mockMvc.perform(get("/api/address")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.doorNumber", is("12A")))
        .andExpect(jsonPath("$.addressLine1", is("Line 1")))
        .andExpect(jsonPath("$.pinCode", is(123456)));

    logger.warn("testGetAddress passed ✅");
  }

  @Test
  @WithMockUser(username = TEST_USERNAME)
  void testAddAddress() throws Exception {
    logger.warn("Running testAddAddress 🚀");

    addressRepository.deleteAllInBatch();

    AddressResponse addressResponse = new AddressResponse(
        "34B",
        "New Line 1",
        "New Line 2",
        654321L,
        "New City"
    );

    mockMvc.perform(post("/api/address")
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(addressResponse)))
        .andExpect(status().isOk())
        .andExpect(content().string("Address added successfully."));

    logger.warn("testAddAddress passed ✅");
  }

  @Test
  @WithMockUser(username = TEST_USERNAME)
  void testUpdateAddress() throws Exception {
    logger.warn("Running testUpdateAddress 🚀");

    AddressResponse updatedResponse = new AddressResponse(
        "99C",
        "Updated Line 1",
        "Updated Line 2",
        789012L,
        "Updated City"
    );

    mockMvc.perform(put("/api/address")
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(updatedResponse)))
        .andExpect(status().isOk())
        .andExpect(content().string("Address updated successfully."));

    logger.warn("testUpdateAddress passed ✅");
  }

  @Test
  @WithMockUser(username = TEST_USERNAME)
  void testDeleteAddress() throws Exception {
    logger.warn("Running testDeleteAddress 🚀");

    mockMvc.perform(delete("/api/address")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().string("Address deleted successfully."));

    logger.warn("testDeleteAddress passed ✅");
  }
}


