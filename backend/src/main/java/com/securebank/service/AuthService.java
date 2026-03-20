package com.securebank.service;

import com.securebank.dto.AuthRequest;
import com.securebank.dto.AuthResponse;
import com.securebank.dto.MfaRequest;
import com.securebank.dto.RegisterRequest;
import com.securebank.model.User;
import com.securebank.repository.UserRepository;
import com.securebank.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private MfaService mfaService;

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 30;

    @Transactional
    public AuthResponse register(RegisterRequest request, String ipAddress) {
        // Check if user already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        // Create new user
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAddress(request.getAddress());
        user.setRole("ROLE_CUSTOMER");
        user.setActive(true);
        user.setFailedLoginAttempts(0);
        user.setLastLoginIp(ipAddress);
        user.setCreatedAt(LocalDateTime.now());

        userRepository.save(user);

        // Generate JWT token
        String token = jwtUtil.generateToken(user);

        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setRole(user.getRole());
        response.setMfaRequired(false);

        return response;
    }

    public AuthResponse login(AuthRequest request, String ipAddress) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        // Check if account is locked
        if (user.getAccountLockedUntil() != null && 
            LocalDateTime.now().isBefore(user.getAccountLockedUntil())) {
            throw new RuntimeException("Account is locked. Try again after " + 
                user.getAccountLockedUntil());
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            handleFailedLogin(user);
            throw new RuntimeException("Invalid credentials");
        }

        // Check if MFA is enabled
        if (user.getMfaEnabled() != null && user.getMfaEnabled()) {
            AuthResponse response = new AuthResponse();
            response.setMfaRequired(true);
            response.setEmail(user.getEmail());
            response.setToken(null);
            response.setFirstName(null);
            response.setLastName(null);
            response.setRole(null);
            return response;
        }

        // Reset failed attempts on successful login
        user.setFailedLoginAttempts(0);
        user.setAccountLockedUntil(null);
        user.setLastLoginAt(LocalDateTime.now());
        user.setLastLoginIp(ipAddress);
        userRepository.save(user);

        // Generate JWT token
        String token = jwtUtil.generateToken(user);

        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setRole(user.getRole());
        response.setMfaRequired(false);

        return response;
    }

    public AuthResponse loginWithMfa(MfaRequest request, String ipAddress) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            handleFailedLogin(user);
            throw new RuntimeException("Invalid credentials");
        }

        // Verify MFA code
        int code = Integer.parseInt(request.getMfaCode());
        if (!mfaService.verifyCode(user.getMfaSecret(), code)) {
            throw new RuntimeException("Invalid MFA code");
        }

        // Reset failed attempts on successful login
        user.setFailedLoginAttempts(0);
        user.setAccountLockedUntil(null);
        user.setLastLoginAt(LocalDateTime.now());
        user.setLastLoginIp(ipAddress);
        userRepository.save(user);

        // Generate JWT token
        String token = jwtUtil.generateToken(user);

        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setRole(user.getRole());
        response.setMfaRequired(false);

        return response;
    }

    @Transactional
    private void handleFailedLogin(User user) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);

        if (attempts >= MAX_FAILED_ATTEMPTS) {
            user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(LOCKOUT_DURATION_MINUTES));
        }

        userRepository.save(user);
    }
}