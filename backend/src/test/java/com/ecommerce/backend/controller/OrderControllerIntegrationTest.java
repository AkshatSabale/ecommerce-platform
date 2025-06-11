package com.ecommerce.backend.controller;




import com.ecommerce.backend.kafka.OrderMessage;
import com.ecommerce.backend.kafka.OrderProducer;
import com.ecommerce.backend.model.Address;
import com.ecommerce.backend.model.Order;
import com.ecommerce.backend.model.OrderItem;
import com.ecommerce.backend.model.OrderStatus;
import com.ecommerce.backend.model.Product;
import com.ecommerce.backend.model.User;
import com.ecommerce.backend.repository.AddressRepository;
import com.ecommerce.backend.repository.OrderRepository;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@AutoConfigureMockMvc
class OrderControllerIntegrationTest extends BaseIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private OrderRepository orderRepository;

  @Autowired
  private AddressRepository addressRepository;

  @Autowired
  private ProductRepository productRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private OrderProducer orderProducer;

  @Autowired
  private EntityManager entityManager;


  private Long orderId;

  @BeforeEach
  @WithMockUser(username = TEST_USERNAME)
  void setupOrder() {
    User user = userRepository.findByUsername(TEST_USERNAME).orElseThrow();

    // Address
    Address address = new Address();
    address.setUserId(user.getId());
    address.setDoorNumber("123 Test St");
    address.setCity("TestCity");
    address.setAddressLine1("TestState");
    address.setPinCode(123456L);
    address.setAddressLine2("TestCountry");
    addressRepository.save(address);

    // Product
    Product product = new Product();
    product.setName("Test Product");
    product.setDescription("Test Description");
    product.setPrice(1000L);
    productRepository.save(product);

    // Order
    Order order = new Order();
    order.setUserId(user.getId());
    order.setStatus(OrderStatus.CONFIRMED);
    order.setCreatedAt(LocalDateTime.now());
    order.setTotalAmount((double) 2000L);

    OrderItem item = new OrderItem();
    item.setProductId(product.getId());
    item.setQuantity(2);
    item.setOrder(order);
    order.setOrderItems(List.of(item));

    orderRepository.save(order);
    this.orderId = order.getId();
  }

  @BeforeEach
  void setupMocks() {
    // Configure the OrderProducer mock to do nothing
    doNothing().when(orderProducer).sendMessage(any(OrderMessage.class));
  }

  @Test
  @WithMockUser(username = TEST_USERNAME)
  void testGetOrderById() throws Exception {
    mockMvc.perform(get("/api/order/" + orderId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(orderId));
  }

  @Test
  @WithMockUser(username = TEST_USERNAME)
  void testCancelOrder() throws Exception {
    mockMvc.perform(patch("/api/order/" + orderId + "/cancel"))
        .andExpect(status().isAccepted())
        .andExpect(content().string("Order cancellation request submitted."));
  }

  @Test
  @WithMockUser(username = TEST_USERNAME)
  void testGetUserOrders() throws Exception {
    mockMvc.perform(get("/api/order"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1));
  }

  @Test
  @WithMockUser(username = TEST_USERNAME)
  void testRequestReturnInvalidStatus() throws Exception {
    String body = """
  {
    "reason": "Wrong item"
  }
  """;

    mockMvc.perform(post("/api/order/" + orderId + "/return")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isBadRequest());// ← expect 400 not 5xx
  }


  @Test
  @WithMockUser(username = TEST_USERNAME, roles = {"ADMIN"})
  void testAdminGetAllOrders() throws Exception {
    mockMvc.perform(get("/api/order/admin/orders")
            .param("page", "0")
            .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(1));
  }
}

