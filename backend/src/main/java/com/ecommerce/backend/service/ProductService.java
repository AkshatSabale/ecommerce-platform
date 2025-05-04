package com.ecommerce.backend.service;
import com.ecommerce.backend.model.Product;
import com.ecommerce.backend.repository.ProductRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

  @Autowired
  private ProductRepository productRepository;

  public List<Product> searchProducts(String searchTerm) {
    return productRepository.searchProducts(searchTerm);
  }
}
