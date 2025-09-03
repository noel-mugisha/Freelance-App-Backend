package com.demo.backend.controller;

import com.demo.backend.model.Role;
import com.demo.backend.model.User;
import com.demo.backend.repository.UserRepository;
import com.demo.backend.security.JwtService;
import com.demo.backend.service.EmailService;
import com.demo.backend.service.OtpService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final OtpService otpService;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtService jwtService, EmailService emailService, OtpService otpService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.emailService = emailService;
        this.otpService = otpService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username already taken"));
        }
        if (userRepository.existsByEmail(req.email())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already registered"));
        }
        Role role = req.role() != null ? req.role() : Role.CLIENT;
        User user = User.builder()
                .username(req.username())
                .email(req.email())
                .passwordHash(passwordEncoder.encode(req.password()))
                .fullName(req.fullName())
                .bio(req.bio())
                .role(role)
                .status("PENDING_VERIFICATION")
                .build();
        User savedUser = userRepository.save(user);

        // Generate and send OTP via email
        String otp = otpService.generateAndSaveOtp(savedUser);
        emailService.sendVerificationOtp(savedUser.getEmail(), otp);

        return new ResponseEntity<>(Map.of("message", "Registration successful. Please check your email for a verification code."), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        User user = userRepository.findByEmail(req.email()).orElse(null);
        if (user != null && "PENDING_VERIFICATION".equals(user.getStatus())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Account not verified. Please check your email for the OTP."));
        }

        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.email(), req.password())
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
            String accessToken = jwtService.generateToken(req.email(), new HashMap<>());
            String refreshToken = jwtService.generateRefreshToken(req.email(), new HashMap<>());
            return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken));
        } catch (org.springframework.security.core.AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Bad credentials"));
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody VerifyOtpRequest req) {
        User user = userRepository.findByEmail(req.email()).orElse(null);

        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found."));
        }

        if (otpService.validateOtp(user, req.otp())) {
            user.setStatus("ACTIVE");
            userRepository.save(user);
            otpService.clearOtp(user); // Clean up the used OTP
            return ResponseEntity.ok(Map.of("message", "Email verified successfully. You can now log in."));
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid or expired OTP."));
        }
    }

    @PostMapping("/request-password-reset")
    public ResponseEntity<?> requestPasswordReset(@Valid @RequestBody ForgotPasswordRequest req) {
        userRepository.findByEmail(req.email()).ifPresent(user -> {
            String otp = otpService.generateAndSaveOtp(user);
            emailService.sendPasswordResetOtp(user.getEmail(), otp);
        });
        // Always return a success message to prevent email enumeration attacks
        return ResponseEntity.ok(Map.of("message", "If an account with that email exists, a password reset code has been sent."));
    }

    @PostMapping("/reset-password-otp")
    public ResponseEntity<?> resetPasswordWithOtp(@Valid @RequestBody ResetPasswordOtpRequest req) {
        User user = userRepository.findByEmail(req.email()).orElse(null);

        if (user == null) {
            // Keep the message generic to prevent user enumeration
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid OTP or email."));
        }

        if (otpService.validateOtp(user, req.otp())) {
            user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
            userRepository.save(user);
            otpService.clearOtp(user); // Clean up the used OTP
            return ResponseEntity.ok(Map.of("message", "Password has been reset successfully."));
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid or expired OTP."));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshRequest req) {
        try {
            String username = jwtService.extractUsername(req.refreshToken());
            // If token is expired or invalid, extractUsername will throw an exception
            String accessToken = jwtService.generateToken(username, new HashMap<>());
            String newRefreshToken = jwtService.generateRefreshToken(username, new HashMap<>());
            return ResponseEntity.ok(new AuthResponse(accessToken, newRefreshToken));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid or expired refresh token"));
        }
    }

    // --- DTOs / Records for Request & Response bodies ---

    public record RegisterRequest(
            @NotBlank @Size(min = 3, max = 50) String username,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 6, max = 100) String password,
            Role role,
            @NotBlank String fullName,
            @NotBlank String bio
    ) {}

    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}

    public record VerifyOtpRequest(@NotBlank @Email String email, @NotBlank @Size(min = 6, max = 6, message = "OTP must be 6 digits") String otp) {}

    public record ForgotPasswordRequest(@NotBlank @Email String email) {}

    public record ResetPasswordOtpRequest(
            @NotBlank @Email String email,
            @NotBlank @Size(min = 6, max = 6, message = "OTP must be 6 digits") String otp,
            @NotBlank @Size(min = 6, max = 100) String newPassword
    ) {}

    public record AuthResponse(String accessToken, String refreshToken) {}

    public record RefreshRequest(@NotBlank String refreshToken) {}
}