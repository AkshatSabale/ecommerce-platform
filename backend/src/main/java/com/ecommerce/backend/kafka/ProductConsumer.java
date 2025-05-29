package com.ecommerce.backend.kafka;

import com.ecommerce.backend.model.Product;
import com.ecommerce.backend.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ProductConsumer {

  @Autowired
  private ProductRepository productRepository;

  @KafkaListener(topics = "product-topic", groupId = "product_group")
  public void consume(String productJson) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      Product product = mapper.readValue(productJson, Product.class);
      productRepository.save(product);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}