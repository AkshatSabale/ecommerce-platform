package com.ecommerce.backend.model;

import jakarta.persistence.*;
import java.util.List;
import java.util.Set;

@Entity
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  private String email;

  @Column(nullable = false)
  private String password;

  private Set<String> roles;

  public User() {
  }

  // constructor (you can change/remove the generic <T> if not needed)
  public User(String email, String password, Set<String> roles) {
    this.email = email;
    this.password = password;
    this.roles = roles;
  }
  // Getters and Setters

  public Long getId() {
    return id;
  }

  public String getEmail() {
    return email;
  }

  public String getPassword() {
    return password;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public Set<String> getRoles() {
    return roles;
  }

  public void setRoles(Set<String> roles) {
    this.roles = roles;
  }
}