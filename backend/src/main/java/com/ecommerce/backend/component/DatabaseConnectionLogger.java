package com.ecommerce.backend.component;

import jakarta.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DatabaseConnectionLogger {

  @Autowired
  private DataSource dataSource;

  @PostConstruct
  public void printConnectionUser() throws SQLException {
    try (Connection connection = dataSource.getConnection()) {
      System.out.println("Connected as: " + connection.getMetaData().getUserName());
    }
  }
}
