package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.AddressResponse;
import com.ecommerce.backend.dto.CartResponse;
import com.ecommerce.backend.model.Address;
import com.ecommerce.backend.model.Cart;
import com.ecommerce.backend.repository.AddressRepository;
import org.springframework.stereotype.Service;

@Service
public class AddressService {

  private final AddressRepository addressRepository;

  public AddressService(AddressRepository addressRepository) {
    this.addressRepository = addressRepository;
  }

  public AddressResponse getAddress(Long userId) {
    return addressRepository.findByUserId(userId)
        .map(this::convertToResponse)
        .orElseThrow(() -> new RuntimeException("No address added, please add one."));
  }

  public void addAddress(Long userId, String addressLine1, String addressLine2,
      String city, String doorNumber, Long pinCode) {
    if (addressRepository.findByUserId(userId).isPresent()) {
      throw new RuntimeException("Address already exists. Use update instead.");
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

  public void updateAddress(Long userId, AddressResponse updatedAddress) {
    Address address = addressRepository.findByUserId(userId)
        .orElseThrow(() -> new RuntimeException("No address found to update."));

    address.setAddressLine1(updatedAddress.getAddressLine1());
    address.setAddressLine2(updatedAddress.getAddressLine2());
    address.setDoorNumber(updatedAddress.getDoorNumber());
    address.setCity(updatedAddress.getCity());
    address.setPinCode(updatedAddress.getPinCode());

    addressRepository.save(address);
  }

  public void deleteAddress(Long userId) {
    Address address = addressRepository.findByUserId(userId)
        .orElseThrow(() -> new RuntimeException("No address found to delete."));
    addressRepository.delete(address);
  }

  private AddressResponse convertToResponse(Address address) {
    return new AddressResponse(
        address.getDoorNumber(),
        address.getAddressLine1(),
        address.getAddressLine2(),
        address.getPinCode(),
        address.getCity()
    );
  }
}
