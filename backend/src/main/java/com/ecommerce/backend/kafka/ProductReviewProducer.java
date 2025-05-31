package com.ecommerce.backend.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ProductReviewProducer {

  @Autowired
  private KafkaTemplate<String, String> kafkaTemplate;

  private final ObjectMapper objectMapper = new ObjectMapper();

  public void sendMessage(ProductReviewMessage message) {
    try {
      String messageJson = objectMapper.writeValueAsString(message);
      kafkaTemplate.send("product-review-topic", messageJson);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Error serializing product review message", e);
    }
  }
}