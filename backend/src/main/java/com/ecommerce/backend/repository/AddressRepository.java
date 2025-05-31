package com.ecommerce.backend.repository;

import com.ecommerce.backend.model.Address;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long>
{
  List<Address> findByUserId(Long userId);
}
