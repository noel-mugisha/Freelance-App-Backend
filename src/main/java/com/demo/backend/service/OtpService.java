package com.demo.backend.service;

import com.demo.backend.model.OtpToken;
import com.demo.backend.model.User;
import com.demo.backend.repository.OtpTokenRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Optional;

@Service
public class OtpService {

    private final OtpTokenRepository otpTokenRepository;

    @Value("${otp.expiration-minutes:10}")
    private int expirationMinutes;

    public OtpService(OtpTokenRepository otpTokenRepository) {
        this.otpTokenRepository = otpTokenRepository;
    }

    @Transactional
    public String generateAndSaveOtp(User user) {
        // Invalidate any existing OTP for this user
        otpTokenRepository.findByUser(user).ifPresent(otpTokenRepository::delete);

        String otp = generateRandomOtp();
        OtpToken otpToken = new OtpToken(user, otp, expirationMinutes);
        otpTokenRepository.save(otpToken);
        return otp;
    }

    /**
     * Validates the provided OTP for a given user.
     * Returns true if valid, false otherwise.
     */
    public boolean validateOtp(User user, String otp) {
        Optional<OtpToken> otpTokenOptional = otpTokenRepository.findByUser(user);
        if (otpTokenOptional.isEmpty()) {
            return false; // No OTP found for this user
        }

        OtpToken otpToken = otpTokenOptional.get();

        // Check for expiry
        if (otpToken.getExpiryDate().isBefore(OffsetDateTime.now())) {
            otpTokenRepository.delete(otpToken); // Clean up expired token
            return false;
        }

        // Check if the token matches
        return otpToken.getToken().equals(otp);
    }

    /**
     * Deletes the OTP for a given user after successful validation.
     */
    @Transactional
    public void clearOtp(User user) {
        otpTokenRepository.findByUser(user).ifPresent(otpTokenRepository::delete);
    }

    private String generateRandomOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000); // Generates a number between 100000 and 999999
        return String.valueOf(otp);
    }
}
