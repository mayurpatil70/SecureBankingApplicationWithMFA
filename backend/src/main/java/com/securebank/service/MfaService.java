package com.securebank.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

@Service
public class MfaService {

    private final GoogleAuthenticator googleAuthenticator = new GoogleAuthenticator();

    public String generateSecretKey() {
        GoogleAuthenticatorKey key = googleAuthenticator.createCredentials();
        return key.getKey();
    }

    public String generateQrCodeUrl(String email, String secret) {
        return GoogleAuthenticatorQRGenerator.getOtpAuthURL(
                "SecureBank",
                email,
                new GoogleAuthenticatorKey.Builder(secret).build()
        );
    }

    public String generateQrCodeImage(String qrCodeUrl) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrCodeUrl, BarcodeFormat.QR_CODE, 300, 300);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            byte[] qrCodeBytes = outputStream.toByteArray();

            return Base64.getEncoder().encodeToString(qrCodeBytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }

    public boolean verifyCode(String secret, int code) {
        return googleAuthenticator.authorize(secret, code);
    }
}