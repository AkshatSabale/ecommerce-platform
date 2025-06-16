package com.ecommerce.backend.model;

import jakarta.persistence.*;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;


@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
public class Payment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id; // Primary key (auto-generated)

  @Column(nullable = true) // Make this explicitly nullable
  private String paymentId; // Razorpay's payment ID (nullable until payment completes)

  @Column(name = "razorpay_order_id", unique = true)
  private String razorpayOrderId;

  @Column(nullable = false)
  private Double amount;

  @Column(nullable = false)
  private String currency;

  @Column(nullable = false)
  private String status;

  private String receipt;
  private String method;
  private String bank;
  private String wallet;
  private String cardId;

  @CreationTimestamp
  private LocalDateTime createdAt;

  @UpdateTimestamp
  private LocalDateTime updatedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = true)
  private User user;


  @Setter
  @OneToOne
  @JoinColumn(name = "order_id", nullable = true)
  private Order order;


  public Payment(String paymentId, String razorpayOrderId, Double amount, String currency, User user) {
    this.paymentId = paymentId;
    this.razorpayOrderId = razorpayOrderId;
    this.amount = amount;
    this.currency = currency;
    this.user = user;
  }
}