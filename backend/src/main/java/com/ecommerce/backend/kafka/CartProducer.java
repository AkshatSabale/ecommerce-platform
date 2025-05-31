package com.ecommerce.backend.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
@RequiredArgsConstructor
public class CartProducer {

  @Autowired
  private KafkaTemplate<String, String> kafkaTemplate;

  private final ObjectMapper objectMapper = new ObjectMapper();

  public void sendMessage(CartMessage message) {
    try {
      String messageJson = objectMapper.writeValueAsString(message);
      kafkaTemplate.send("cart-topic", messageJson);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Error serializing cart message", e);
    }
  }
}