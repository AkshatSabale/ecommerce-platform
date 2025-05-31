package com.ecommerce.backend.kafka;

import com.ecommerce.backend.dto.ReturnRequestDto;
import com.ecommerce.backend.model.Order;
import com.ecommerce.backend.model.OrderItem;
import com.ecommerce.backend.model.OrderStatus;
import com.ecommerce.backend.model.Product;
import com.ecommerce.backend.repository.OrderRepository;
import com.ecommerce.backend.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.AccessDeniedException;
import java.util.NoSuchElementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderConsumer {

  @Autowired
  private OrderRepository orderRepository;

  @Autowired
  private ProductRepository productRepository;

  @KafkaListener(topics = "order-topic", groupId = "order_group")
  public void consume(String messageJson) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      OrderMessage message = mapper.readValue(messageJson, OrderMessage.class);

      switch (message.getOperation()) {
        case "CREATE":
          orderRepository.save(message.getOrder());
          break;
        case "UPDATE":
          orderRepository.findById(message.getOrder().getId()).ifPresent(order -> {
            // Update order fields as needed
            orderRepository.save(order);
          });
          break;
        case "CANCEL":
          orderRepository.findById(message.getOrderId()).ifPresent(order -> {
            order.setStatus(OrderStatus.CANCELLED);
            restockItems(order);
            orderRepository.save(order);
          });
          break;
        case "RETURN_REQUEST":
          orderRepository.findById(message.getOrderId()).ifPresent(order -> {
            order.setStatus(OrderStatus.RETURN_REQUESTED);
            orderRepository.save(order);
          });
          break;
        case "APPROVE_RETURN":
          orderRepository.findById(message.getOrderId()).ifPresent(order -> {
            order.setStatus(OrderStatus.RETURNED);
            restockItems(order);
            orderRepository.save(order);
          });
          break;
        case "CONFIRM":
          orderRepository.findById(message.getOrderId()).ifPresent(order -> {
            order.setStatus(OrderStatus.CONFIRMED);
            for (OrderItem item : order.getOrderItems()) {
              productRepository.findById(item.getProductId()).ifPresent(product -> {
                product.setQuantity(product.getQuantity() - item.getQuantity());
                productRepository.save(product);
              });
            }
            orderRepository.save(order);
          });
          break;
        default:
          System.out.println("Unknown operation: " + message.getOperation());
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void restockItems(Order order) {
    for (OrderItem item : order.getOrderItems()) {
      productRepository.findById(item.getProductId()).ifPresent(product -> {
        product.setQuantity(product.getQuantity() + item.getQuantity());
        productRepository.save(product);
      });
    }
  }
}