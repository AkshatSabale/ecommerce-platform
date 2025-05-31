package com.ecommerce.backend.kafka;

import com.ecommerce.backend.dto.AddressResponse;
import com.ecommerce.backend.model.Address;
import com.ecommerce.backend.repository.AddressRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AddressConsumer {

  @Autowired
  private AddressRepository addressRepository;

  @KafkaListener(topics = "address-topic", groupId = "address_group")
  public void consume(String messageJson) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      AddressMessage message = mapper.readValue(messageJson, AddressMessage.class);

      String operation = message.getOperation();
      AddressResponse response = message.getAddressResponse();
      Long userId = message.getUserId();

      switch (operation) {
        case "CREATE":
          Address newAddress = new Address();
          newAddress.setUserId(userId);
          newAddress.setAddressLine1(response.getAddressLine1());
          newAddress.setAddressLine2(response.getAddressLine2());
          newAddress.setDoorNumber(response.getDoorNumber());
          newAddress.setPinCode(response.getPinCode());
          newAddress.setCity(response.getCity());
          addressRepository.save(newAddress);
          break;

        case "UPDATE":
          List<Address> addresses = addressRepository.findByUserId(userId);
          if (!addresses.isEmpty()) {
            Address addressToUpdate = addresses.get(0);
            addressToUpdate.setAddressLine1(response.getAddressLine1());
            addressToUpdate.setAddressLine2(response.getAddressLine2());
            addressToUpdate.setDoorNumber(response.getDoorNumber());
            addressToUpdate.setPinCode(response.getPinCode());
            addressToUpdate.setCity(response.getCity());
            addressRepository.save(addressToUpdate);
          }
          break;

        case "DELETE":
          List<Address> toDelete = addressRepository.findByUserId(userId);
          if (!toDelete.isEmpty()) {
            addressRepository.delete(toDelete.get(0));
          }
          break;

        default:
          System.out.println("Unknown operation: " + operation);
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
