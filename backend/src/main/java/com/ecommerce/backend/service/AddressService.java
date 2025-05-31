package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.AddressResponse;
import com.ecommerce.backend.dto.CartResponse;
import com.ecommerce.backend.kafka.AddressMessage;
import com.ecommerce.backend.kafka.AddressProducer;
import com.ecommerce.backend.model.Address;
import com.ecommerce.backend.model.Cart;
import com.ecommerce.backend.repository.AddressRepository;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class AddressService {

  private final AddressRepository addressRepository;
  private final AddressProducer addressProducer;

  public AddressService(AddressRepository addressRepository, AddressProducer addressProducer) {
    this.addressRepository = addressRepository;
    this.addressProducer = addressProducer;
  }

  // Caches the address for a specific userId
  @Cacheable(value = "address", key = "#userId")
  public AddressResponse getAddress(Long userId) {
    List<Address> addresses = addressRepository.findByUserId(userId);
    if (addresses.isEmpty()) {
      throw new NoSuchElementException("No address added, please add one.");
    }
    Address address = addresses.get(0);
    return new AddressResponse(
        address.getDoorNumber(),
        address.getAddressLine1(),
        address.getAddressLine2(),
        address.getPinCode(),
        address.getCity()
    );
  }

  // Evicts the cached address for the userId
  @CacheEvict(value = "address", key = "#userId")
  public void addAddress(Long userId, AddressResponse addressResponse) {
    AddressMessage addressMessage = new AddressMessage();
    addressMessage.setOperation("CREATE");
    addressMessage.setUserId(userId);
    addressMessage.setAddressResponse(addressResponse);
    addressProducer.sendMessage(addressMessage);
  }

  @CacheEvict(value = "address", key = "#userId")
  public void updateAddress(Long userId, AddressResponse addressResponse) {
    AddressMessage addressMessage = new AddressMessage();
    addressMessage.setOperation("UPDATE");
    addressMessage.setUserId(userId);
    addressMessage.setAddressResponse(addressResponse);
    addressProducer.sendMessage(addressMessage);
  }

  @CacheEvict(value = "address", key = "#userId")
  public void deleteAddress(Long userId) {
    AddressMessage addressMessage = new AddressMessage();
    addressMessage.setOperation("DELETE");
    addressMessage.setUserId(userId);
    addressProducer.sendMessage(addressMessage);
  }
}