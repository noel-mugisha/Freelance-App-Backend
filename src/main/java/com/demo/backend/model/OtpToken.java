package com.demo.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "otp_tokens")
public class OtpToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String token; // This will store the 6-digit OTP

    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id", unique = true) // A user can have only one active OTP
    private User user;

    @Column(nullable = false)
    private OffsetDateTime expiryDate;

    public OtpToken(User user, String token, int expirationMinutes) {
        this.user = user;
        this.token = token;
        this.expiryDate = OffsetDateTime.now().plusMinutes(expirationMinutes);
    }
}
