package com.ecommerce.backend.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class AddressProducer
{
  @Autowired
  private KafkaTemplate<String, String> kafkaTemplate;

  private final ObjectMapper objectMapper = new ObjectMapper();

  public void sendMessage(AddressMessage message) {
    try {
      String messageJson = objectMapper.writeValueAsString(message);
      kafkaTemplate.send("address-topic", messageJson);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Error serializing address message", e);
    }
  }
}
