package com.securebank.service;

import com.securebank.model.PasswordResetToken;
import com.securebank.model.User;
import com.securebank.repository.PasswordResetTokenRepository;
import com.securebank.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PasswordResetService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final int EXPIRATION_HOURS = 24;

    @Transactional
    public String createPasswordResetToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        // Delete any existing unused tokens for this user
        tokenRepository.deleteByUser(user);

        // Generate random token
        String token = UUID.randomUUID().toString();

        // Create new token
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(EXPIRATION_HOURS));
        resetToken.setUsed(false);

        tokenRepository.save(resetToken);

        // In production, send email here
        // For now, we'll return the token (in production, never return it!)
        System.out.println("Password Reset Token: " + token);
        System.out.println("Reset Link: http://localhost:3000/reset-password?token=" + token);

        return token;
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired reset token"));

        if (resetToken.isUsed()) {
            throw new RuntimeException("This reset token has already been used");
        }

        if (resetToken.isExpired()) {
            throw new RuntimeException("This reset token has expired");
        }

        // Update password
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Mark token as used
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
    }

    public boolean validateToken(String token) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElse(null);

        return resetToken != null && !resetToken.isUsed() && !resetToken.isExpired();
    }
}