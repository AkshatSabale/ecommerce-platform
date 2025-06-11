package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.LoginUserDto;
import com.ecommerce.backend.dto.RegisterUserDto;
import com.ecommerce.backend.dto.VerifyUserDto;
import com.ecommerce.backend.model.Role;
import com.ecommerce.backend.model.User;
import com.ecommerce.backend.repository.RoleRepository;
import com.ecommerce.backend.repository.UserRepository;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest  {

  @Mock
  private UserRepository userRepository;

  @Mock
  private AuthenticationManager authenticationManager;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private EmailService emailService;

  @Mock
  private RoleRepository roleRepository;

  @InjectMocks
  private AuthenticationService authenticationService;

  @Test
  public void signup_ValidInput_CreatesUserWithVerificationCode()
      throws MessagingException, MessagingException {
    // Arrange
    RegisterUserDto registerUserDto = new RegisterUserDto();
    registerUserDto.setUsername("testuser");
    registerUserDto.setEmail("test@example.com");
    registerUserDto.setPassword("password");

    Role customerRole = new Role();
    customerRole.setName("ROLE_CUSTOMER");
    when(roleRepository.findByName("ROLE_CUSTOMER")).thenReturn(Optional.of(customerRole));
    when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    User result = authenticationService.signup(registerUserDto);

    // Assert
    assertNotNull(result);
    assertEquals("testuser", result.getUsername());
    assertEquals("test@example.com", result.getEmail());
    assertEquals("encodedPassword", result.getPassword());
    assertNotNull(result.getVerificationCode());
    assertNotNull(result.getVerificationCodeExpiresAt());
    assertFalse(result.isEnabled());
    verify(emailService).sendVerificationEmail(anyString(), anyString(), anyString());
  }

  @Test
  public void authenticate_ValidCredentials_ReturnsUser() {
    // Arrange
    LoginUserDto loginUserDto = new LoginUserDto();
    loginUserDto.setEmail("test@example.com");
    loginUserDto.setPassword("password");

    User user = new User();
    user.setEmail("test@example.com");
    user.setPassword("encodedPassword");
    user.setEnabled(true);

    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(null);

    // Act
    User result = authenticationService.authenticate(loginUserDto);

    // Assert
    assertNotNull(result);
    assertEquals("test@example.com", result.getEmail());
  }

  @Test
  public void authenticate_UnverifiedAccount_ThrowsException() {
    // Arrange
    LoginUserDto loginUserDto = new LoginUserDto();
    loginUserDto.setEmail("test@example.com");
    loginUserDto.setPassword("password");

    User user = new User();
    user.setEmail("test@example.com");
    user.setPassword("encodedPassword");
    user.setEnabled(false);

    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

    // Act & Assert
    assertThrows(RuntimeException.class, () -> authenticationService.authenticate(loginUserDto));
  }

  @Test
  public void verifyUser_ValidCode_VerifiesUser() {
    // Arrange
    VerifyUserDto verifyUserDto = new VerifyUserDto();
    verifyUserDto.setEmail("test@example.com");
    verifyUserDto.setVerificationCode("123456");

    User user = new User();
    user.setEmail("test@example.com");
    user.setVerificationCode("123456");
    user.setVerificationCodeExpiresAt(LocalDateTime.now().plusHours(1));
    user.setEnabled(false);

    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
    when(userRepository.save(any(User.class))).thenReturn(user);

    // Act
    authenticationService.verifyUser(verifyUserDto);

    // Assert
    assertTrue(user.isEnabled());
    assertNull(user.getVerificationCode());
    assertNull(user.getVerificationCodeExpiresAt());
  }

  @Test
  public void verifyUser_ExpiredCode_ThrowsException() {
    // Arrange
    VerifyUserDto verifyUserDto = new VerifyUserDto();
    verifyUserDto.setEmail("test@example.com");
    verifyUserDto.setVerificationCode("123456");

    User user = new User();
    user.setEmail("test@example.com");
    user.setVerificationCode("123456");
    user.setVerificationCodeExpiresAt(LocalDateTime.now().minusHours(1));
    user.setEnabled(false);

    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

    // Act & Assert
    assertThrows(RuntimeException.class, () -> authenticationService.verifyUser(verifyUserDto));
  }

  @Test
  public void resendVerificationCode_ForUnverifiedUser_SendsNewCode() throws MessagingException {
    // Arrange
    String email = "test@example.com";
    User user = new User();
    user.setEmail(email);
    user.setVerificationCode("oldcode");
    user.setVerificationCodeExpiresAt(LocalDateTime.now().minusHours(1));
    user.setEnabled(false);

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    when(userRepository.save(any(User.class))).thenReturn(user);

    // Act
    authenticationService.resendVerificationCode(email);

    // Assert
    assertNotEquals("oldcode", user.getVerificationCode());
    assertTrue(user.getVerificationCodeExpiresAt().isAfter(LocalDateTime.now()));
    verify(emailService).sendVerificationEmail(anyString(), anyString(), anyString());
  }

  @Test
  public void resendVerificationCode_ForVerifiedUser_ThrowsException() {
    // Arrange
    String email = "test@example.com";
    User user = new User();
    user.setEmail(email);
    user.setEnabled(true);

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

    // Act & Assert
    assertThrows(RuntimeException.class, () -> authenticationService.resendVerificationCode(email));
  }
}