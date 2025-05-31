package com.ecommerce.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "orders")
public class Order {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long userId;

  private Double totalAmount;

  @Enumerated(EnumType.STRING)
  private OrderStatus status; // Enum like PENDING, CONFIRMED, SHIPPED, CANCELLED

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL,fetch = FetchType.EAGER)
  private List<OrderItem> orderItems = new ArrayList<>();

  private LocalDateTime createdAt;

  @Enumerated(EnumType.STRING)
  private PaymentMethod paymentMethod;

  private String doorNumber;
  private String addressLine1;
  private String addressLine2;
  private Long pinCode;
  private String city;
}
