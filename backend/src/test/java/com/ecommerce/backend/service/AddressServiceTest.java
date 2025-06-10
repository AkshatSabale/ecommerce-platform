package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.AddressResponse;
import com.ecommerce.backend.kafka.AddressProducer;
import com.ecommerce.backend.model.Address;
import com.ecommerce.backend.repository.AddressRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

  @InjectMocks
  private AddressService addressService;

  @Mock
  private AddressRepository addressRepository;

  @Mock
  private AddressProducer addressProducer;

  private static final Logger logger = LoggerFactory.getLogger(AddressServiceTest.class);

  @BeforeEach
  void setUp() {
    logger.warn("Running AddressServiceTest setUp 🚀");
  }

  @Test
  void testGetAddress_Found() {
    logger.warn("Running testGetAddress_Found 🚀");

    Address address = new Address();
    address.setUserId(1L);
    address.setDoorNumber("12A");
    address.setAddressLine1("Line 1");
    address.setAddressLine2("Line 2");
    address.setPinCode(123456L);
    address.setCity("Test City");

    when(addressRepository.findByUserId(1L)).thenReturn(List.of(address));

    AddressResponse response = addressService.getAddress(1L);

    assertEquals("12A", response.getDoorNumber());
    assertEquals("Line 1", response.getAddressLine1());
    assertEquals("Line 2", response.getAddressLine2());
    assertEquals(123456L, response.getPinCode());
    assertEquals("Test City", response.getCity());

    logger.warn("testGetAddress_Found passed ✅");
  }

  @Test
  void testGetAddress_NotFound() {
    logger.warn("Running testGetAddress_NotFound 🚀");

    when(addressRepository.findByUserId(9999L)).thenReturn(List.of());

    assertThrows(NoSuchElementException.class, () -> addressService.getAddress(9999L));

    logger.warn("testGetAddress_NotFound passed ✅");
  }

  @Test
  void testAddAddress() {
    logger.warn("Running testAddAddress 🚀");

    AddressResponse addressResponse = new AddressResponse(
        "34B",
        "Line A",
        "Line B",
        654321L,
        "CityX"
    );

    addressService.addAddress(1L, addressResponse);

    verify(addressProducer, times(1)).sendMessage(any());

    logger.warn("testAddAddress passed ✅");
  }

  @Test
  void testUpdateAddress() {
    logger.warn("Running testUpdateAddress 🚀");

    AddressResponse addressResponse = new AddressResponse(
        "99Z",
        "Line U",
        "Line V",
        987654L,
        "UpdatedCity"
    );

    addressService.updateAddress(1L, addressResponse);

    verify(addressProducer, times(1)).sendMessage(any());

    logger.warn("testUpdateAddress passed ✅");
  }

  @Test
  void testDeleteAddress() {
    logger.warn("Running testDeleteAddress 🚀");

    addressService.deleteAddress(1L);

    verify(addressProducer, times(1)).sendMessage(any());

    logger.warn("testDeleteAddress passed ✅");
  }
}

