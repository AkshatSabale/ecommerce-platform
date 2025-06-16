package com.ecommerce.backend.controller;


import com.ecommerce.backend.model.Role;
import com.ecommerce.backend.model.User;
import com.ecommerce.backend.repository.RoleRepository;
import com.ecommerce.backend.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;


@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Rollback
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect" // Explicit dialect
})
public abstract class BaseIntegrationTest {

  @Autowired
  protected UserRepository userRepository;

  @Autowired
  protected RoleRepository roleRepository;

  @Autowired
  protected EntityManager entityManager;

  protected final String TEST_USERNAME = "testuser";

  @BeforeEach
  public void setupBaseUser() {
    // Clear DB first
    userRepository.deleteAllInBatch();
    roleRepository.deleteAllInBatch();
    entityManager.clear();

    // Create roles
    Role customerRole = new Role();
    customerRole.setName("ROLE_CUSTOMER");
    roleRepository.save(customerRole);

    Role adminRole = new Role();
    adminRole.setName("ROLE_ADMIN");
    roleRepository.save(adminRole);

    // Create test user
    User user = new User();
    user.setUsername(TEST_USERNAME);
    user.setEmail("testuser@example.com");
    user.setPassword("password"); // You can encode it if your app requires
    user.setEnabled(true);
    user.getRoles().add(customerRole);
    user.getRoles().add(adminRole);

    userRepository.save(user);
    entityManager.flush();
    entityManager.clear();
  }
}
