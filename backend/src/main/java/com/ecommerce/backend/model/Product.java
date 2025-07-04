package com.ecommerce.backend.model;

import jakarta.persistence.*;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "products")
public class Product implements Serializable {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private Long quantity;
  private String name;
  private Double price;
  private String description;
  @Column(name = "image_filename")
  private String imageFilename;


  public Product() {}

  public Product(Long id, String name, double price,Long quantity) {
    this.id = id;
    this.name = name;
    this.price = price;
    this.quantity=quantity;
  }

  public double getPrice() {
    return price;
  }

  public void setPrice(double price) {
    this.price = price;
  }
}