package com.ecommerce.backend.dto;

import jakarta.persistence.Column;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductResponse implements Serializable
{
  private Long quantity;
  private String name;
  private Double price;
  private String description;
  private String imageFilename;
}
