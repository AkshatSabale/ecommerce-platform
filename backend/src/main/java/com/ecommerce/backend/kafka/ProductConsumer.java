package com.ecommerce.backend.kafka;

import com.ecommerce.backend.model.Product;
import com.ecommerce.backend.repository.CartRepository;
import com.ecommerce.backend.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ProductConsumer {

  @Autowired
  private ProductRepository productRepository;

  @Autowired
  private CartRepository cartRepository;

  @KafkaListener(topics = "product-topic", groupId = "product_group")
  public void consume(String messageJson) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      ProductMessage message = mapper.readValue(messageJson, ProductMessage.class);

      switch (message.getOperation()) {
        case "CREATE":
          productRepository.save(message.getProduct());
          break;
        case "UPDATE":
          productRepository.findById(message.getProduct().getId()).ifPresent(product -> {
            product.setName(message.getProduct().getName());
            product.setPrice(message.getProduct().getPrice());
            product.setQuantity(message.getProduct().getQuantity());
            product.setImageFilename(message.getProduct().getImageFilename());
            productRepository.save(product);
          });
          break;
        case "DELETE":
          if (message.getProductId() != null) {
            cartRepository.deleteCartItemsByProductId(message.getProductId());
            productRepository.deleteById(message.getProductId());
          }
          break;
        default:
          System.out.println("Unknown operation: " + message.getOperation());
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}