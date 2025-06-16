package com.ecommerce.backend.kafka;

import com.ecommerce.backend.model.Product;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
public class ProductProducer {

  @Autowired
  private KafkaTemplate<String, String> kafkaTemplate;

  private final ObjectMapper objectMapper = new ObjectMapper();

  public void sendMessage(ProductMessage message) {
    try {
      String messageJson = objectMapper.writeValueAsString(message);
      kafkaTemplate.send("product-topic", messageJson);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Error serializing product message", e);
    }
  }
}