package com.securebank.service;

import com.securebank.dto.ChangePasswordRequest;
import com.securebank.dto.ProfileRequest;
import com.securebank.model.User;
import com.securebank.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.securebank.dto.MfaResponse;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MfaService mfaService;

    public User getUserProfile(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public User updateProfile(Long userId, ProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAddress(request.getAddress());

        return userRepository.save(user);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        // Set new password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional
public MfaResponse enableMfa(Long userId) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

    if (user.getMfaEnabled() != null && user.getMfaEnabled()) {
        throw new RuntimeException("MFA is already enabled");
    }

    // Generate secret
    String secret = mfaService.generateSecretKey();
    user.setMfaSecret(secret);
    user.setMfaEnabled(true);
    userRepository.save(user);

    // Generate QR code
    String qrCodeUrl = mfaService.generateQrCodeUrl(user.getEmail(), secret);
    String qrCodeImage = mfaService.generateQrCodeImage(qrCodeUrl);

    MfaResponse response = new MfaResponse();
    response.setSecret(secret);
    response.setQrCodeUrl(qrCodeUrl);
    response.setQrCodeImage(qrCodeImage);

    return response;
}

@Transactional
public void disableMfa(Long userId, String mfaCode) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

    if (user.getMfaEnabled() == null || !user.getMfaEnabled()) {
        throw new RuntimeException("MFA is not enabled");
    }

    // Verify MFA code before disabling
    int code = Integer.parseInt(mfaCode);
    if (!mfaService.verifyCode(user.getMfaSecret(), code)) {
        throw new RuntimeException("Invalid MFA code");
    }

    user.setMfaEnabled(false);
    user.setMfaSecret(null);
    userRepository.save(user);
}

public boolean verifyMfaCode(Long userId, String mfaCode) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

    if (user.getMfaEnabled() == null || !user.getMfaEnabled()) {
        return true; // MFA not enabled, allow login
    }

    int code = Integer.parseInt(mfaCode);
    return mfaService.verifyCode(user.getMfaSecret(), code);
}
}