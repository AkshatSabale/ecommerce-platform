package com.ecommerce.backend.repository;

import com.ecommerce.backend.dto.TopProductDTO;
import com.ecommerce.backend.model.Order;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
  List<Order> findByUserId(Long userId);

  @Query("""
    SELECT new com.ecommerce.backend.dto.TopProductDTO(
        oi.productId,
        SUM(oi.quantity)
    )
    FROM Order o
    JOIN o.orderItems oi
    WHERE o.createdAt >= :fromDate
    GROUP BY oi.productId
    ORDER BY SUM(oi.quantity) DESC
    """)
  List<TopProductDTO> findTopSellingProductsSince(
      @Param("fromDate") LocalDateTime fromDate,
      Pageable pageable);


  @Query("""
    SELECT new com.ecommerce.backend.dto.TopProductDTO(
        oi.productId,
        SUM(oi.quantity)
    )
    FROM Order o
    JOIN o.orderItems oi
    GROUP BY oi.productId
    ORDER BY SUM(oi.quantity) DESC
    """)
  List<TopProductDTO> findTopSellingProductsAllTime(Pageable pageable);


  @Query("""
    SELECT SUM(o.totalAmount)
    FROM Order o
    WHERE o.createdAt >= :fromDate
      AND o.status = com.ecommerce.backend.model.OrderStatus.CONFIRMED
    """)
  Optional<Double> findTotalRevenueSince(@Param("fromDate") LocalDateTime fromDate);
}
