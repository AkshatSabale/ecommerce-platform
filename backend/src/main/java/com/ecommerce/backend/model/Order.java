package com.ecommerce.backend.model;

import com.fasterxml.jackson.annotation.JsonFormat;
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

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime createdAt;

  @Enumerated(EnumType.STRING)
  private PaymentMethod paymentMethod;

  @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
  private Payment payment;

  public void setPayment(Payment payment) {
    this.payment = payment;
    if (payment != null && payment.getOrder() != this) {
      payment.setOrder(this);
    }
  }


  private String doorNumber;
  private String addressLine1;
  private String addressLine2;
  private Long pinCode;
  private String city;
}
