package com.ecommerce.backend.controller;




import com.ecommerce.backend.dto.LoginUserDto;
import com.ecommerce.backend.dto.RegisterUserDto;
import com.ecommerce.backend.dto.VerifyUserDto;
import com.ecommerce.backend.model.User;
import com.ecommerce.backend.responses.LoginResponse;
import com.ecommerce.backend.service.AuthenticationService;
import com.ecommerce.backend.service.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/auth")
@RestController
public class AuthenticationController {
  private final JwtService jwtService;

  private final AuthenticationService authenticationService;

  public AuthenticationController(JwtService jwtService, AuthenticationService authenticationService) {
    this.jwtService = jwtService;
    this.authenticationService = authenticationService;
  }



  @GetMapping("/check-admin")
  public ResponseEntity<Boolean> checkAdmin() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    boolean isAdmin = auth.getAuthorities().stream()
        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
    return ResponseEntity.ok(isAdmin);
  }

  @PostMapping("/signup")
  public ResponseEntity<User> register(@RequestBody RegisterUserDto registerUserDto) {
    User registeredUser = authenticationService.signup(registerUserDto);
    return ResponseEntity.ok(registeredUser);
  }

  @PostMapping("/login")
  public ResponseEntity<?> authenticate(@RequestBody LoginUserDto loginUserDto) {
    try {
      User authenticatedUser = authenticationService.authenticate(loginUserDto);
      String jwtToken = jwtService.generateToken(authenticatedUser);
      LoginResponse loginResponse = new LoginResponse(jwtToken, jwtService.getExpirationTime());
      return ResponseEntity.ok(loginResponse);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @PostMapping("/verify")
  public ResponseEntity<?> verifyUser(@RequestBody VerifyUserDto verifyUserDto) {
    try {
      authenticationService.verifyUser(verifyUserDto);
      return ResponseEntity.ok("Account verified successfully");
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @PostMapping("/resend")
  public ResponseEntity<?> resendVerificationCode(@RequestParam String email) {
    try {
      authenticationService.resendVerificationCode(email);
      return ResponseEntity.ok("Verification code sent");
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }


  @PostMapping("/logout")
  public ResponseEntity<String> logout(HttpServletRequest request) {
    // Optionally extract token and log it or blacklist it if required
    String token = extractToken(request);
    // log.info("User logging out with token: {}", token);

    return ResponseEntity.ok("Logged out successfully.");
  }

  private String extractToken(HttpServletRequest request) {
    String authHeader = request.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      return authHeader.substring(7);
    }
    return null;
  }
}

