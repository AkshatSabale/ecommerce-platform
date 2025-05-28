package com.ecommerce.backend.service;
import com.ecommerce.backend.model.Product;
import com.ecommerce.backend.repository.ProductRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

  @Autowired
  private ProductRepository productRepository;

  @Cacheable(value = "products")
  public List<Product> getAllProducts() {
    return productRepository.findAll();
  }

  public Product createProduct(Product product) {
    return productRepository.save(product);
  }

  public ResponseEntity<String> deleteProduct(long id) {
    if (productRepository.existsById(id)) {
      productRepository.deleteById(id);
      return ResponseEntity.ok("Deleted successfully");
    } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");
    }
  }

  @CacheEvict(value = "products", allEntries = true)
  public ResponseEntity<String> updateProduct(long id, Product updatedProduct) {
    return productRepository.findById(id)
        .map(product -> {
          product.setName(updatedProduct.getName());
          product.setPrice(updatedProduct.getPrice());
          product.setQuantity(updatedProduct.getQuantity());
          product.setImageFilename(updatedProduct.getImageFilename());
          productRepository.save(product);
          return ResponseEntity.ok("Product updated successfully");
        })
        .orElseGet(() ->
            ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product with ID " + id + " not found")
        );
  }

  public List<Product> searchProducts(String searchTerm) {
    return productRepository.searchProducts(searchTerm);
  }
}
