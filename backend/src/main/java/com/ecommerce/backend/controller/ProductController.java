package com.ecommerce.backend.controller;

import com.ecommerce.backend.model.Product;
import com.ecommerce.backend.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductRepository repository;
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);


    public ProductController(ProductRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Product> getAll() {
        logger.info("Fetching all products");
        return repository.findAll();
    }

    @PostMapping
    public Product create(@RequestBody Product product) {
        logger.info("Creating product:{}",product.getName());
        return repository.save(product);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@RequestBody long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            logger.info("Deleted product with id {}", id);
            return ResponseEntity.ok("Deleted successfully");
        } else {
            logger.warn("Attempted to delete non-existent product id {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateProduct(@PathVariable long id, @RequestBody Product updatedProduct) {
        return repository.findById(id)
            .map(product -> {
                product.setName(updatedProduct.getName());
                product.setPrice(updatedProduct.getPrice());
                product.setQuantity(updatedProduct.getQuantity());
                repository.save(product);
                logger.info("Product with ID:{} updated",id);
                return ResponseEntity.ok("Product updated successfully");
            })
            .orElseGet(() -> {
                logger.warn("Product with ID:{} not found for update", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product with ID " + id + " not found");
            });

    }
}