package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.AddressResponse;
import com.ecommerce.backend.dto.CartResponse;
import com.ecommerce.backend.model.Address;
import com.ecommerce.backend.model.Cart;
import com.ecommerce.backend.repository.AddressRepository;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;

@Service
public class AddressService {

  private final AddressRepository addressRepository;

  public AddressService(AddressRepository addressRepository) {
    this.addressRepository = addressRepository;
  }

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

  public void addAddress(Long userId, String addressLine1, String addressLine2, String city, String doorNumber, Long pinCode) {
    List<Address> existing = addressRepository.findByUserId(userId);
    if (!existing.isEmpty()) {
      throw new IllegalStateException("Address already exists. Please update instead.");
    }
    Address address = new Address();
    address.setUserId(userId);
    address.setAddressLine1(addressLine1);
    address.setAddressLine2(addressLine2);
    address.setCity(city);
    address.setDoorNumber(doorNumber);
    address.setPinCode(pinCode);
    addressRepository.save(address);
  }

  public void updateAddress(Long userId, AddressResponse addressResponse) {
    List<Address> addresses = addressRepository.findByUserId(userId);
    if (addresses.isEmpty()) {
      throw new NoSuchElementException("No address to update. Please add one first.");
    }
    Address address = addresses.get(0);
    address.setAddressLine1(addressResponse.getAddressLine1());
    address.setAddressLine2(addressResponse.getAddressLine2());
    address.setCity(addressResponse.getCity());
    address.setDoorNumber(addressResponse.getDoorNumber());
    address.setPinCode(addressResponse.getPinCode());
    addressRepository.save(address);
  }

  public void deleteAddress(Long userId) {
    List<Address> addresses = addressRepository.findByUserId(userId);
    if (addresses.isEmpty()) {
      throw new NoSuchElementException("No address to delete.");
    }
    addressRepository.delete(addresses.get(0));
  }
}