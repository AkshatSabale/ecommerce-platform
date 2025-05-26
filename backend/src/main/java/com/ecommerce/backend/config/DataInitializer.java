package com.ecommerce.backend.config;

import com.ecommerce.backend.model.Role;
import com.ecommerce.backend.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class DataInitializer implements CommandLineRunner {
  @Autowired
  private RoleRepository roleRepository;

  @Override
  public void run(String... args) {
    if (roleRepository.findByName("ROLE_ADMIN").isEmpty()) {
      roleRepository.save(new Role(null, "ROLE_ADMIN"));
    }
    if (roleRepository.findByName("ROLE_CUSTOMER").isEmpty()) {
      roleRepository.save(new Role(null, "ROLE_CUSTOMER"));
    }
  }
}
