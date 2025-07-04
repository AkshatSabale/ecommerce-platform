package com.ecommerce.backend.repository;

import com.ecommerce.backend.model.Payment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

  List<Payment> findByUser_Id(Long userId);// OK

  Page<Payment> findByUser_Id(Long userId, Pageable pageable);

  Optional<Payment> findByRazorpayOrderId(String razorpayOrderId); // FIXED name

  Optional<Payment> findByRazorpayOrderIdAndUser_Id(String razorpayOrderId, Long userId); // FIXED both parts
}