package com.ecommerce.backend.kafka;

import com.ecommerce.backend.dto.ReturnRequestDto;
import com.ecommerce.backend.model.Order;
import com.ecommerce.backend.model.OrderItem;
import com.ecommerce.backend.model.OrderStatus;
import com.ecommerce.backend.model.Product;
import com.ecommerce.backend.model.User;
import com.ecommerce.backend.repository.OrderRepository;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderConsumer {

  @Autowired
  private OrderRepository orderRepository;

  @Autowired
  private ProductRepository productRepository;

  private  UserRepository userRepository;

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
        case "CONFIRM_ORDER":
          orderRepository.findById(message.getOrderId()).ifPresent(order -> {
            order.setStatus(OrderStatus.CONFIRMED);
            restockItems(order);
            orderRepository.save(order);
          });
          break;
        case "SHIP_ORDER":
          orderRepository.findById(message.getOrderId()).ifPresent(order -> {
            order.setStatus(OrderStatus.SHIPPED);
            orderRepository.save(order);
          });
          break;
        case "DELIVER_ORDER":
          orderRepository.findById(message.getOrderId()).ifPresent(order -> {
            order.setStatus(OrderStatus.DELIVERED);
            for (OrderItem item : order.getOrderItems()) {
              productRepository.findById(item.getProductId())
                  .flatMap(product -> userRepository.findById(message.getUserId()))
                  .ifPresent(user -> {
                    List<Long> list = user.getProductsPurchased();
                    if (list == null) {
                      list = new ArrayList<>();
                      user.setProductsPurchased(list);
                    }
                    list.add(item.getProductId());
                    userRepository.save(user); // Don't forget to save the user!
                  });
            }
            orderRepository.save(order);
          });
        case "COMPLETE_RETURN":
          orderRepository.findById(message.getOrderId()).ifPresent(order -> {
            order.setStatus(OrderStatus.RETURNED);
            restockItems(order);
            orderRepository.save(order);
          });
          break;
        case "APPROVE_RETURN":
          orderRepository.findById(message.getOrderId()).ifPresent(order -> {
            order.setStatus(OrderStatus.RETURN_APPROVED);
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
        if (order.getStatus() == OrderStatus.CANCELLED ||
            order.getStatus() == OrderStatus.RETURNED) {
          product.setQuantity(product.getQuantity() + item.getQuantity());
          productRepository.save(product);
        }
        else if(order.getStatus() == OrderStatus.CONFIRMED)
        {
          product.setQuantity(product.getQuantity() - item.getQuantity());
          productRepository.save(product);
        }
      });
    }
  }
}