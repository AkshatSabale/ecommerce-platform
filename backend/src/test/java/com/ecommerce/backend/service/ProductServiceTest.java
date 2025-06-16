package com.ecommerce.backend.service;

import com.ecommerce.backend.kafka.ProductProducer;
import com.ecommerce.backend.model.Product;
import com.ecommerce.backend.dto.ProductResponse;
import com.ecommerce.backend.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import java.util.*;
import org.springframework.test.annotation.Rollback;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Rollback
class ProductServiceTest {

  @InjectMocks
  private ProductService productService;

  @Mock
  private ProductProducer productProducer;

  @Mock
  private ProductRepository productRepository;

  private static final Logger logger = LoggerFactory.getLogger(ProductServiceTest.class);



  @BeforeEach
  void setUp() {
    logger.warn("Running ProductServiceTest setUp 🚀");
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testGetProductById_Found() {
    logger.warn("Running testGetProductById_Found 🚀");

    Product product = new Product(1L, "Test Product", 99.99, 10L);
    product.setDescription("Test Desc");
    product.setImageFilename("test.png");

    when(productRepository.findById(1L)).thenReturn(Optional.of(product));

    ProductResponse response = productService.getProductById(1L);

    assertEquals("Test Product", response.getName());
    assertEquals(99.99, response.getPrice());
    assertEquals(10L, response.getQuantity());
    assertEquals("Test Desc", response.getDescription());
    assertEquals("test.png", response.getImageFilename());

    logger.warn("testGetProductById_Found passed ✅");
  }

  @Test
  void testGetProductById_NotFound() {
    logger.warn("Running testGetProductById_NotFound 🚀");

    when(productRepository.findById(2L)).thenReturn(Optional.empty());

    ProductResponse response = productService.getProductById(2L);

    assertNull(response.getName());

    logger.warn("testGetProductById_NotFound passed ✅");
  }

  @Test
  void testGetAllProducts() {
    logger.warn("Running testGetAllProducts 🚀");

    List<Product> productList = Arrays.asList(
        new Product(1L, "Product 1", 10.0, 5L),
        new Product(2L, "Product 2", 20.0, 8L)
    );

    when(productRepository.findAll()).thenReturn(productList);

    List<Product> result = productService.getAllProducts();

    assertEquals(2, result.size());
    verify(productRepository, times(1)).findAll();

    logger.warn("testGetAllProducts passed ✅");
  }

  @Test
  void testCreateProductAsync() {
    logger.warn("Running testCreateProductAsync 🚀");

    Product product = new Product(1L, "New Product", 50.0, 15L);

    productService.createProductAsync(product);

    verify(productProducer, times(1)).sendMessage(any());

    logger.warn("testCreateProductAsync passed ✅");
  }

  @Test
  void testUpdateProduct() {
    logger.warn("Running testUpdateProduct 🚀");

    Product updatedProduct = new Product(1L, "Updated Product", 55.0, 12L);

    ResponseEntity<String> response = productService.updateProduct(1L, updatedProduct);

    verify(productProducer, times(1)).sendMessage(any());
    assertEquals("Product update request submitted.", response.getBody());

    logger.warn("testUpdateProduct passed ✅");
  }

  @Test
  void testDeleteProduct() {
    logger.warn("Running testDeleteProduct 🚀");

    ResponseEntity<String> response = productService.deleteProduct(1L);

    verify(productProducer, times(1)).sendMessage(any());
    assertEquals("Product delete request submitted.", response.getBody());

    logger.warn("testDeleteProduct passed ✅");
  }
}