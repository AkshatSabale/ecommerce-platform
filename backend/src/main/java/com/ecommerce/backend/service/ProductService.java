package com.ecommerce.backend.service;
import com.ecommerce.backend.kafka.ProductMessage;
import com.ecommerce.backend.kafka.ProductProducer;
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
  private ProductProducer productProducer;
  @Autowired
  private ProductRepository productRepository;

  @Cacheable(value = "products", key = "'all'")
  public List<Product> getAllProducts() {
    return productRepository.findAll();
  }

  @Cacheable(value = "products", key = "'all'")
  public void createProductAsync(Product product) {
    ProductMessage message = new ProductMessage();
    message.setOperation("CREATE");
    message.setProduct(product);
    productProducer.sendMessage(message);
  }

  @CacheEvict(value = "products", key = "#id")
  public ResponseEntity<String> updateProduct(long id, Product updatedProduct) {
    updatedProduct.setId(id); // Ensure ID is set
    ProductMessage message = new ProductMessage();
    message.setOperation("UPDATE");
    message.setProduct(updatedProduct);
    productProducer.sendMessage(message);
    return ResponseEntity.ok("Product update request submitted.");
  }

  @CacheEvict(value = "products", key = "#id")
  public ResponseEntity<String> deleteProduct(long id) {
    ProductMessage message = new ProductMessage();
    message.setOperation("DELETE");
    message.setProductId(id);
    productProducer.sendMessage(message);
    return ResponseEntity.ok("Product delete request submitted.");
  }

  public List<Product> searchProducts(String searchTerm) {
    return productRepository.searchProducts(searchTerm);
  }
}
