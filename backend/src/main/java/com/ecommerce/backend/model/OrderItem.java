package com.ecommerce.backend.model;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "order_items")
public class OrderItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "order_id")
  private Order order; // link back to parent order

  private Long productId;
  private Integer quantity;
  private Double price;       // price per unit when order was placed
  private Double totalPrice;  // price * quantity

  // getters and setters
}
