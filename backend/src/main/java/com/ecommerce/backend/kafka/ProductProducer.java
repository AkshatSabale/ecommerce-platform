package com.ecommerce.backend.kafka;

import com.ecommerce.backend.model.Product;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ProductProducer {
  @Autowired
  private KafkaTemplate<String, String> kafkaTemplate;

  private final ObjectMapper objectMapper = new ObjectMapper();

  public void sendProduct(Product product) {
    try {
      String productJson = objectMapper.writeValueAsString(product);
      kafkaTemplate.send("product-topic", productJson);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Error serializing product", e);
    }
  }
}