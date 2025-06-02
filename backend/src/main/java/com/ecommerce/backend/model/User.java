package com.ecommerce.backend.model;



import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "users")
@Getter
@Setter
public class User implements UserDetails {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;
  @Column(unique = true, nullable = false)
  private String username;
  @Column(unique = true, nullable = false)
  private String email;
  @Column(nullable = false)
  private String password;

  private List<Long> productsPurchased;

  @Column(name = "verification_code")
  private String verificationCode;
  @Column(name = "verification_expiration")
  private LocalDateTime verificationCodeExpiresAt;
  private boolean enabled;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "user_roles",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id")
  )
  private Set<Role> roles = new HashSet<>();

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return roles;
  }

  //constructor for creating an unverified user
  public User(String username, String email, String password) {
    this.username = username;
    this.email = email;
    this.password = password;
  }
  //default constructor
  public User(){
  }


  @Override
  public String getUsername() {
    return username;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true; // or implement logic
  }

  @Override
  public boolean isAccountNonLocked() {
    return true; // or implement logic
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true; // or implement logic
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }
  //TODO: add proper boolean checks

}
