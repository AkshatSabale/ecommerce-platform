package com.ecommerce.backend.model;

import java.util.Collection;
import java.util.List;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public abstract class CustomUserDetails implements UserDetails {
  @Getter
  private Long id;
  private String username;


  // ... other UserDetails implementations

}