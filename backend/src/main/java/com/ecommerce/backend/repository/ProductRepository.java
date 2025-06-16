package com.ecommerce.backend.repository;

import com.ecommerce.backend.model.Product;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

  @Query(value = "SELECT * FROM products WHERE to_tsvector('english', name || ' ' || coalesce(description, '')) @@ plainto_tsquery('english', :searchTerm)", nativeQuery = true)
  List<Product> searchProducts(@Param("searchTerm") String searchTerm);
}