package com.demo.backend.config;

import com.demo.backend.model.Role;
import com.demo.backend.model.User;
import com.demo.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminUserInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public CommandLineRunner initAdminUser() {
        return args -> {
            // Check if admin user already exists
            if (userRepository.findByUsername("admin").isEmpty()) {
                // Create admin user
                User admin = User.builder()
                        .username("admin")
                        .email("admin@example.com")
                        .passwordHash(passwordEncoder.encode("admin"))
                        .fullName("Administrator")
                        .role(Role.ADMIN)
                        .status("ACTIVE")
                        .build();
                
                userRepository.save(admin);
                System.out.println("Admin user created successfully!");
            }
        };
    }
}
