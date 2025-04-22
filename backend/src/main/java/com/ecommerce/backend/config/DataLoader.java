package com.ecommerce.backend.config;

import com.ecommerce.backend.model.User;
import com.ecommerce.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Set;

@Configuration
public class DataLoader {

  @Bean
  public CommandLineRunner createTestUser(UserRepository userRepository) {
    return args -> {
      if (userRepository.findByEmail("testuser@akshat.com").isEmpty()) {
        User user = new User();
        user.setEmail("testuser@akshat.com");
        user.setPassword(new BCryptPasswordEncoder().encode("test123")); // securely encoded
        user.setRoles(Set.of("ROLE_USER"));

        userRepository.save(user);
        System.out.println("✅ Test user created: testuser@akshat.com / test123");
      } else {
        System.out.println("ℹ️ Test user already exists");
      }
    };
  }
}
