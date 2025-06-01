package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.ProductResponse;
import com.ecommerce.backend.model.Product;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductService productService;

    @GetMapping
    public List<Product> getAll() {
        logger.info("Fetching all products");
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable long id) {
        logger.info("Fetching product with ID: {}", id);
        ProductResponse response = productService.getProductById(id);

        if (response.getName() == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(response);
    }


    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> create(@RequestBody Product product) {
        logger.info("Sending product to Kafka: {}", product.getName());
        productService.createProductAsync(product);
        return ResponseEntity.ok("Product creation request submitted.");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> delete(@PathVariable long id) {
        logger.info("Request to delete product with ID: {}", id);
        return productService.deleteProduct(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updateProduct(@PathVariable long id, @RequestBody Product updatedProduct) {
        logger.info("Request to update product with ID: {}", id);
        return productService.updateProduct(id, updatedProduct);
    }

    @GetMapping("/search")
    public List<Product> searchProducts(@RequestParam String query) {
        logger.info("Searching products with query: {}", query);
        return productService.searchProducts(query);
    }
}